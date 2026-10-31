package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.common.lock.LockHandle;
import com.pethome.common.lock.RedisDistributedLock;
import com.pethome.entity.Appointment;
import com.pethome.mapper.AppointmentMapper;
import com.pethome.service.appointment.AppointmentService;
import com.pethome.service.common.RedisCacheService;
import com.pethome.service.appointment.StaffScheduleService;
import com.pethome.service.admin.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private RedisDistributedLock distributedLock;

    @Autowired
    private StaffScheduleService staffScheduleService;

    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 时间段锁 key 前缀（分布式锁）
     */
    private static final String SLOT_LOCK_PREFIX = "appointment:slot:lock:";
    private static final String APPOINTMENT_CACHE_PREFIX = "appointment:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment createAppointment(Appointment appointment) {
        // 验证必填字段
        if (appointment.getServiceType() == null || appointment.getServiceType().trim().isEmpty()) {
            throw new RuntimeException("服务类型不能为空");
        }
        if (appointment.getDate() == null) {
            throw new RuntimeException("预约日期不能为空");
        }
        if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
            throw new RuntimeException("时间段不能为空");
        }
        if (appointment.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        // 时间段并发控制：分布式锁削峰（快速失败）+ 数据库唯一索引兜底（最终裁决）
        String slotToken = buildSlotToken(appointment);
        String lockKey = SLOT_LOCK_PREFIX + slotToken;
        LockHandle lockHandle = distributedLock.tryLock(lockKey, Duration.ofSeconds(30), Duration.ofSeconds(2));
        if (lockHandle == null) {
            throw new RuntimeException("该时间段正在被预约，请稍后重试");
        }

        try {
            // 再次检查数据库（双重检查）
            int conflictCount = appointmentMapper.countByServiceTypeAndDateTime(
                        appointment.getServiceType(),
                appointment.getDate(),
                appointment.getTimeSlot(),
                appointment.getMemberId()
            );
            
            if (conflictCount > 0) {
                throw new RuntimeException("该时间段已被预约，请选择其他时间");
            }
            
            // 设置默认值
            if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
                appointment.setStatus("pending"); // 待分配
            }
            if (appointment.getAppointmentDate() == null && appointment.getDate() != null) {
                // 从 date 和 timeSlot 构建 appointmentDate
                String[] timeParts = appointment.getTimeSlot().split("-");
                if (timeParts.length >= 1) {
                    String startTime = timeParts[0].trim();
                    LocalDateTime appointmentDateTime = LocalDate.parse(
                        appointment.getDate().toString()
                    ).atStartOfDay().plusHours(
                        Integer.parseInt(startTime.split(":")[0])
                    ).plusMinutes(
                        startTime.contains(":") && startTime.split(":").length > 1 
                            ? Integer.parseInt(startTime.split(":")[1]) : 0
                    );
                    appointment.setAppointmentDate(appointmentDateTime);
                    }
            }
            if (appointment.getCreateTime() == null) {
                appointment.setCreateTime(LocalDateTime.now());
            }
            
            // 自动分配服务人员（如果未指定memberId）
            if (appointment.getMemberId() == null) {
                Long assignedStaffId = autoAssignStaff(appointment);
                if (assignedStaffId != null) {
                    appointment.setMemberId(assignedStaffId);
                    appointment.setStatus("assigned"); // 已分配
            }
            }
            
            // 落库前重算令牌（成员可能在自动分配后被赋值）
            appointment.setSlotToken(buildSlotToken(appointment));

            // 保存预约（uk_apt_slot_token 唯一索引做并发兜底）
            appointmentMapper.insert(appointment);
            
            // 缓存预约信息（30分钟）
            String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + appointment.getId();
            redisCacheService.setCache(appointmentCacheKey, appointment, 30);
            
            return appointment;
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突：锁失效窗口内另一请求已完成落库
            log.warn("时间段唯一索引冲突，判定已被预约: slotToken={}", slotToken);
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        } finally {
            lockHandle.close();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment updateAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            throw new RuntimeException("预约ID不能为空");
        }

        appointmentMapper.updateById(appointment);

        // 改期 / 改服务人员后，占用令牌必须随最终值重算，否则旧时段被错误占用、新时段失去唯一索引保护
        Appointment persisted = appointmentMapper.selectById(appointment.getId());
        if (persisted != null) {
            UpdateWrapper<Appointment> uw = new UpdateWrapper<>();
            uw.eq("id", persisted.getId());
            if (isTerminalStatus(persisted.getStatus())) {
                uw.set("slot_token", null);
                persisted.setSlotToken(null);
            } else {
                String token = buildSlotToken(persisted);
                uw.set("slot_token", token);
                persisted.setSlotToken(token);
            }
            try {
                appointmentMapper.update(null, uw);
            } catch (DuplicateKeyException e) {
                log.warn("改期后时间段唯一索引冲突: appointmentId={}", persisted.getId());
                throw new RuntimeException("目标时间段已被预约，请选择其他时间");
            }
        }

        // 更新缓存
        if (persisted != null) {
            String cacheKey = APPOINTMENT_CACHE_PREFIX + persisted.getId();
            redisCacheService.setCache(cacheKey, persisted, 30);
        }
        return persisted;
    }

    /** 终态：不再占用时段（唯一索引允许多个 NULL） */
    private boolean isTerminalStatus(String status) {
        return "completed".equals(status) || "cancelled".equals(status) || "no_show".equals(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            return null;
        }
        
            appointment.setStatus(status);
            appointmentMapper.updateById(appointment);
        
        // 已完成/已取消/爽约：清空占用令牌，数据库唯一索引随之释放该时间段
        if ("completed".equals(status) || "cancelled".equals(status) || "no_show".equals(status)) {
            UpdateWrapper<Appointment> uw = new UpdateWrapper<>();
            uw.eq("id", id).set("slot_token", null);
            appointmentMapper.update(null, uw);
            appointment.setSlotToken(null);
        }
        
        // 更新缓存
        String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + id;
        redisCacheService.setCache(appointmentCacheKey, appointment, 30);
        
        return appointment;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        // 走缓存读取模板：空值缓存防穿透 + SETNX 互斥重建防击穿 + 随机 TTL 防雪崩
        String cacheKey = APPOINTMENT_CACHE_PREFIX + id;
        return redisCacheService.getOrLoad(cacheKey, 30, Appointment.class,
                () -> appointmentMapper.selectById(id));
    }
    
    @Override
    public IPage<Appointment> getAppointmentList(Page<Appointment> page) {
        return appointmentMapper.selectPage(page, null);
    }

    @Override
    public List<Appointment> getUserAppointments(Long userId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("appointment_date");
        return appointmentMapper.selectList(queryWrapper);
    }

    @Override
    public List<Appointment> getAppointmentsByMemberId(Long memberId) {
        return appointmentMapper.selectByMemberId(memberId);
    }
    
    @Override
    public List<Appointment> getAppointmentsByServiceTypeAndDate(String serviceType, LocalDate date) {
        return appointmentMapper.selectByServiceTypeAndDate(serviceType, date);
    }
    
    @Override
    public boolean checkTimeSlotAvailable(String serviceType, LocalDate date, String timeSlot, Long memberId) {
        // 以数据库为准：Redis 中的时段状态可能因实例重启、任务延迟而滞后，
        // 可用性判定必须读取唯一索引对应的真实占用记录。
        int count = appointmentMapper.countByServiceTypeAndDateTime(serviceType, date, timeSlot, memberId);
        return count == 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment assignAppointment(Long appointmentId, Long memberId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 检查该时间段是否对该服务人员可用
        if (!checkTimeSlotAvailable(
            appointment.getServiceType(),
            appointment.getDate(),
            appointment.getTimeSlot(),
            memberId
        )) {
            throw new RuntimeException("该时间段已被其他预约占用");
        }
        
        // 分配工单：令牌随 service_type/date/time_slot/member 重新计算并落库，
        // 唯一索引会阻止同一服务人员被并发分配到同一时段
        appointment.setMemberId(memberId);
        appointment.setStatus("assigned"); // 已分配
        appointment.setSlotToken(buildSlotToken(appointment));
        appointmentMapper.updateById(appointment);

        // 更新缓存
        String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + appointmentId;
        redisCacheService.setCache(appointmentCacheKey, appointment, 30);
        
        return appointment;
    }
    
    @Override
    public List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentMapper.selectByDateRange(startDate, endDate);
    }
    
    @Override
    public Appointment getAppointmentByVerifyCode(String verifyCode) {
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("verify_code", verifyCode.trim());
        return appointmentMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment verifyAppointment(String verifyCode) {
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            return null;
        }
        
        // 根据核销码查询预约
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("verify_code", verifyCode.trim());
        Appointment appointment = appointmentMapper.selectOne(queryWrapper);
        
        if (appointment != null && appointment.getIsVerified() != null && appointment.getIsVerified() == 0) {
            // 核销成功，更新状态
            appointment.setIsVerified(1);
            appointment.setVerifyTime(LocalDateTime.now());
            appointment.setUpdateTime(LocalDateTime.now());
            // 更新状态为已完成
            if (!"completed".equals(appointment.getStatus())) {
                appointment.setStatus("completed");
            }
            appointmentMapper.updateById(appointment);

            // 核销完成即释放时段：清空占用令牌（唯一索引允许多个 NULL）
            UpdateWrapper<Appointment> uw = new UpdateWrapper<>();
            uw.eq("id", appointment.getId()).set("slot_token", null);
            appointmentMapper.update(null, uw);
            appointment.setSlotToken(null);

            // 更新缓存
            String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + appointment.getId();
            redisCacheService.setCache(appointmentCacheKey, appointment, 30);

            return appointment;
        }
        
        return null; // 无效或已核销
    }
    
    /**
     * 自动分配服务人员
     * 根据排班表选择工作量最少的可用staff
     */
    private Long autoAssignStaff(Appointment appointment) {
        try {
            // 1. 根据 serviceType 查找对应的 departmentId
            Long departmentId = getDepartmentIdByServiceType(appointment.getServiceType());
            if (departmentId == null) {
                log.warn("未找到服务类型对应的部门: {}", appointment.getServiceType());
                return null;
            }
            
            // 2. 解析时间段
            LocalTime startTime = null;
            LocalTime endTime = null;
            if (appointment.getTimeSlot() != null && !appointment.getTimeSlot().isEmpty()) {
                String[] timeParts = appointment.getTimeSlot().split("-");
                if (timeParts.length >= 2) {
                    startTime = LocalTime.parse(timeParts[0].trim());
                    endTime = LocalTime.parse(timeParts[1].trim());
                } else if (timeParts.length == 1) {
                    startTime = LocalTime.parse(timeParts[0].trim());
                    endTime = startTime.plusHours(1); // 默认1小时
                }
            }
            
            if (startTime == null || endTime == null) {
                log.warn("无法解析时间段: {}", appointment.getTimeSlot());
                return null;
            }
            
            // 3. 查找可用的staff列表
            List<Long> availableStaffIds = staffScheduleService.findAvailableStaffIds(
                departmentId, 
                appointment.getDate(), 
                startTime, 
                endTime
            );
            
            if (availableStaffIds == null || availableStaffIds.isEmpty()) {
                log.warn("未找到可用的服务人员，部门ID: {}, 日期: {}", departmentId, appointment.getDate());
                return null;
            }
            
            // 4. 选择工作量最少的staff
            Long assignedStaffId = chooseStaffByLeastWorkload(availableStaffIds, appointment.getDate(), startTime);
            
            if (assignedStaffId != null) {
                log.info("自动分配服务人员成功，staffId: {}", assignedStaffId);
            }

            return assignedStaffId;
        } catch (Exception e) {
            log.error("自动分配服务人员失败", e);
            return null;
        }
    }
    
    /**
     * 根据服务类型获取部门ID
     */
    private Long getDepartmentIdByServiceType(String serviceType) {
        try {
            // 服务类型映射
            String departmentName = null;
            if ("door-cleaning".equals(serviceType) || "上门铲屎".equals(serviceType)) {
                departmentName = "上门铲屎";
            } else if ("grooming".equals(serviceType) || "宠物洗护".equals(serviceType)) {
                departmentName = "宠物洗护";
            } else if ("hospital".equals(serviceType) || "宠物医院".equals(serviceType)) {
                departmentName = "宠物医院";
            }
            
            if (departmentName != null) {
                // 通过部门名称查询
                try {
                    // 获取所有部门，然后查找匹配的
                    List<com.pethome.entity.Department> departments = departmentService.getAllDepartments();
                    for (com.pethome.entity.Department dept : departments) {
                        if (departmentName.equals(dept.getName())) {
                            return dept.getId();
                        }
                    }
                } catch (Exception e) {
                    log.error("查询部门失败", e);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取部门ID失败", e);
            return null;
        }
    }
    
    /**
     * 选择工作量最少的staff
     */
    private Long chooseStaffByLeastWorkload(List<Long> staffIds, LocalDate date, LocalTime startTime) {
        if (staffIds == null || staffIds.isEmpty()) {
            return null;
        }
        
        if (staffIds.size() == 1) {
            return staffIds.get(0);
        }
        
        // 查询每个staff的工作量
        Long minWorkloadStaffId = null;
        int minWorkload = Integer.MAX_VALUE;
        
        for (Long staffId : staffIds) {
            // 统计该staff在当前时间段的预约数量
            int workload = countAppointmentsByStaffAndTime(staffId, date, startTime);
            
            if (workload < minWorkload) {
                minWorkload = workload;
                minWorkloadStaffId = staffId;
            }
        }
        
        return minWorkloadStaffId;
    }
    
    /**
     * 统计员工在指定时间段的工作量
     */
    private int countAppointmentsByStaffAndTime(Long staffId, LocalDate date, LocalTime startTime) {
        try {
            QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("member_id", staffId);
            queryWrapper.eq("date", date);
            queryWrapper.like("time_slot", startTime.toString());
            queryWrapper.notIn("status", "cancelled", "completed");
            return Math.toIntExact(appointmentMapper.selectCount(queryWrapper));
        } catch (Exception e) {
            log.error("统计工作量失败", e);
            return 0;
        }
    }
    
    /**
     * 生成时间段占用令牌：与数据库唯一索引 uk_apt_slot_token 一一对应。
     * 成员为空时使用 “any” 占位，保证未分配时也不会绕过唯一约束。
     */
    private String buildSlotToken(Appointment appointment) {
        String dateStr = appointment.getDate() == null ? "null" : appointment.getDate().format(DateTimeFormatter.ISO_DATE);
        String memberStr = appointment.getMemberId() != null ? appointment.getMemberId().toString() : "any";
        String slot = appointment.getTimeSlot() == null ? "null" : appointment.getTimeSlot().trim();
        return appointment.getServiceType() + "|" + dateStr + "|" + slot + "|" + memberStr;
    }
}
