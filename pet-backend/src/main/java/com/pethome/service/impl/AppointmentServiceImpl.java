package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Appointment;
import com.pethome.mapper.AppointmentMapper;
import com.pethome.service.AppointmentService;
import com.pethome.service.RedisCacheService;
import com.pethome.service.StaffScheduleService;
import com.pethome.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预约服务实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private StaffScheduleService staffScheduleService;

    @Autowired
    private DepartmentService departmentService;
    
    /**
     * Redis 缓存 key 前缀
     */
    private static final String TIME_SLOT_CACHE_PREFIX = "appointment:timeslot:";
    private static final String APPOINTMENT_CACHE_PREFIX = "appointment:";

    @Override
    @Transactional
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
        
        // 检查时间段是否可用（使用 Redis 锁防止并发冲突）
        String cacheKey = generateTimeSlotCacheKey(
            appointment.getServiceType(), 
            appointment.getDate(), 
            appointment.getTimeSlot(),
            appointment.getMemberId()
        );
        
        // 尝试获取 Redis 锁（5秒过期）
        Boolean lockAcquired = redisCacheService.setIfAbsent(cacheKey, "locked", 5, TimeUnit.SECONDS);
        if (lockAcquired == null || !lockAcquired) {
            throw new RuntimeException("该时间段已被占用，请选择其他时间");
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
            
            // 保存预约
            appointmentMapper.insert(appointment);
            
            // 缓存预约信息（30分钟）
            String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + appointment.getId();
            redisCacheService.setCache(appointmentCacheKey, appointment, 30);
            
            // 保持时间段锁（直到预约完成或取消）
            redisCacheService.setCache(cacheKey, appointment.getId().toString(), 24, TimeUnit.HOURS);
            
            return appointment;
        } catch (Exception e) {
            // 释放锁
            redisCacheService.deleteCache(cacheKey);
            throw e;
        }
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            throw new RuntimeException("预约ID不能为空");
        }
        
        appointmentMapper.updateById(appointment);
        
        // 更新缓存
        String cacheKey = APPOINTMENT_CACHE_PREFIX + appointment.getId();
        redisCacheService.setCache(cacheKey, appointment, 30);
        
        return appointmentMapper.selectById(appointment.getId());
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            return null;
        }
        
            appointment.setStatus(status);
            appointmentMapper.updateById(appointment);
        
        // 如果状态是已完成或已取消，释放时间段锁
        if ("completed".equals(status) || "cancelled".equals(status)) {
            String cacheKey = generateTimeSlotCacheKey(
                appointment.getServiceType(),
                appointment.getDate(),
                appointment.getTimeSlot(),
                appointment.getMemberId()
            );
            redisCacheService.deleteCache(cacheKey);
        }
        
        // 更新缓存
        String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + id;
        redisCacheService.setCache(appointmentCacheKey, appointment, 30);
        
        return appointment;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        // 先尝试从缓存获取
        String cacheKey = APPOINTMENT_CACHE_PREFIX + id;
        Appointment cached = redisCacheService.getCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment != null) {
            // 写入缓存
            redisCacheService.setCache(cacheKey, appointment, 30);
        }
        return appointment;
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
        // 先检查 Redis 缓存
        String cacheKey = generateTimeSlotCacheKey(serviceType, date, timeSlot, memberId);
        String cached = redisCacheService.getCache(cacheKey);
        if (cached != null && !"locked".equals(cached)) {
            // 缓存中有预约ID，说明该时间段已被占用
            return false;
        }
        
        // 检查数据库
        int count = appointmentMapper.countByServiceTypeAndDateTime(serviceType, date, timeSlot, memberId);
        return count == 0;
    }
    
    @Override
    @Transactional
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
        
        // 分配工单
        appointment.setMemberId(memberId);
        appointment.setStatus("assigned"); // 已分配
        appointmentMapper.updateById(appointment);
        
        // 更新时间段锁
        String cacheKey = generateTimeSlotCacheKey(
            appointment.getServiceType(),
            appointment.getDate(),
            appointment.getTimeSlot(),
            memberId
        );
        redisCacheService.setCache(cacheKey, appointmentId.toString(), 24, TimeUnit.HOURS);
        
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
    @Transactional
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
            
            // 更新缓存
            String appointmentCacheKey = APPOINTMENT_CACHE_PREFIX + appointment.getId();
            redisCacheService.setCache(appointmentCacheKey, appointment, 30);
            
            // 释放时间段锁
            String cacheKey = generateTimeSlotCacheKey(
                appointment.getServiceType(),
                appointment.getDate(),
                appointment.getTimeSlot(),
                appointment.getMemberId()
            );
            redisCacheService.deleteCache(cacheKey);
            
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
                System.out.println("未找到服务类型对应的部门: " + appointment.getServiceType());
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
                System.out.println("无法解析时间段: " + appointment.getTimeSlot());
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
                System.out.println("未找到可用的服务人员，部门ID: " + departmentId + ", 日期: " + appointment.getDate());
                return null;
            }
            
            // 4. 选择工作量最少的staff
            Long assignedStaffId = chooseStaffByLeastWorkload(availableStaffIds, appointment.getDate(), startTime);
            
            if (assignedStaffId != null) {
                System.out.println("自动分配服务人员成功，staffId: " + assignedStaffId);
            }
            
            return assignedStaffId;
        } catch (Exception e) {
            System.err.println("自动分配服务人员失败: " + e.getMessage());
            e.printStackTrace();
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
                    System.err.println("查询部门失败: " + e.getMessage());
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("获取部门ID失败: " + e.getMessage());
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
            System.err.println("统计工作量失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 生成时间段缓存 key
     */
    private String generateTimeSlotCacheKey(String serviceType, LocalDate date, String timeSlot, Long memberId) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        String memberStr = memberId != null ? memberId.toString() : "any";
        return TIME_SLOT_CACHE_PREFIX + serviceType + ":" + dateStr + ":" + timeSlot + ":" + memberStr;
    }
}
