package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Admin;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.ServiceMemberMapper;
import com.pethome.mapper.ServiceScheduleMapper;
import com.pethome.service.RedisCacheService;
import com.pethome.service.ServiceMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务人员Service实现类
 */
@Service
public class ServiceMemberServiceImpl implements ServiceMemberService {

    @Autowired
    private ServiceMemberMapper memberMapper;

    @Autowired
    private ServiceScheduleMapper scheduleMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    /** 排班/预约按 hospital 查时，同时包含历史上用「宠物医疗」写入的 service_member，保证两店已分配人员都能出现 */
    private static final String HOSPITAL_SERVICE_TYPE_ALIAS = "宠物医疗";

    @Override
    public List<ServiceMember> getMembersByServiceType(String serviceType) {
        List<ServiceMember> list = new ArrayList<>(memberMapper.selectByServiceType(serviceType));
        if ("hospital".equals(serviceType)) {
            mergeByMemberId(list, memberMapper.selectByServiceType(HOSPITAL_SERVICE_TYPE_ALIAS));
        }
        return list;
    }

    @Override
    public List<ServiceMember> getMembersByServiceTypeAndStore(String serviceType, Long storeId) {
        if (storeId == null) {
            return getMembersByServiceType(serviceType);
        }
        List<ServiceMember> list = new ArrayList<>(memberMapper.selectByServiceTypeAndStore(serviceType, storeId));
        if ("hospital".equals(serviceType)) {
            mergeByMemberId(list, memberMapper.selectByServiceTypeAndStore(HOSPITAL_SERVICE_TYPE_ALIAS, storeId));
        }
        return list;
    }

    private void mergeByMemberId(List<ServiceMember> into, List<ServiceMember> from) {
        Set<Long> ids = into.stream().map(ServiceMember::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        for (ServiceMember m : from) {
            if (m.getId() != null && !ids.contains(m.getId())) {
                into.add(m);
                ids.add(m.getId());
            }
        }
    }

    @Override
    @Transactional
    public ServiceMember autoAssignMember(String serviceType, LocalDate date, String timeSlot, Long storeId) {
        // 按门店过滤：storeId 不为 null 时只从该门店或平台级(store_id is null)成员中选
        List<ServiceMember> members = getMembersByServiceTypeAndStore(serviceType, storeId);
        if (members.isEmpty()) {
            return null;
        }

        List<ServiceSchedule> availableSchedules = scheduleMapper.selectAvailableSchedules(
                date, timeSlot, serviceType, storeId);

        if (availableSchedules.isEmpty()) {
            return null;
        }

        ServiceMember bestMember = selectBestMember(members, availableSchedules, date);
        return bestMember;
    }

    /**
     * 选择最佳服务人员
     * 策略：
     * 1. 该时间段可用
     * 2. 当日任务数最少
     * 3. 评分最高
     * 4. 如果任务数相同，选择最后一次接单时间最久者（total_tasks最少者优先）
     */
    private ServiceMember selectBestMember(List<ServiceMember> members, 
                                          List<ServiceSchedule> availableSchedules, 
                                          LocalDate date) {
        
        // 筛选出在可用时间段中的成员
        List<Long> availableMemberIds = availableSchedules.stream()
                .map(ServiceSchedule::getMemberId)
                .collect(Collectors.toList());

        List<ServiceMember> availableMembers = members.stream()
                .filter(m -> availableMemberIds.contains(m.getId()))
                .collect(Collectors.toList());

        if (availableMembers.isEmpty()) {
            return null;
        }

        // 按优先级排序：
        // 1. 当日任务数最少（从Redis缓存或数据库查询）
        // 2. 评分最高
        // 3. 总任务数最少（优先分配给经验较少的成员，平衡工作量）
        ServiceMember bestMember = availableMembers.stream()
                .min(Comparator
                        .comparing((ServiceMember m) -> getMemberWorkload(m.getId(), date))
                        .thenComparing((ServiceMember m) -> m.getRating() != null ? 
                                m.getRating().negate() : java.math.BigDecimal.ZERO)
                        .thenComparing((ServiceMember m) -> m.getTotalTasks() != null ? 
                                m.getTotalTasks() : 0))
                .orElse(null);

        return bestMember;
    }

    @Override
    public int getMemberWorkload(Long memberId, LocalDate date) {
        // 先从Redis缓存获取（如果Redis可用）
        try {
            String cacheKey = String.format("member:workload:%d:%s", memberId, 
                    date.format(DateTimeFormatter.ISO_DATE));
            
            Integer cachedWorkload = (Integer) redisCacheService.getCache(cacheKey);
            if (cachedWorkload != null) {
                return cachedWorkload;
            }
        } catch (Exception e) {
            // Redis连接失败，降级到数据库查询
            System.err.println("Redis缓存查询失败，降级到数据库查询: " + e.getMessage());
        }

        // 从数据库查询（分表统计，避免某表缺 member_id 时整段失败）
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        int workload = 0;
        workload += safeCount(() -> memberMapper.countDoorCleaningTasksByMemberAndDate(memberId, dateStr));
        workload += safeCount(() -> memberMapper.countGroomingTasksByMemberAndDate(memberId, dateStr));
        workload += safeCount(() -> memberMapper.countHospitalTasksByMemberAndDate(memberId, dateStr));

        // 尝试缓存30分钟（如果Redis可用）
        try {
            String cacheKey = String.format("member:workload:%d:%s", memberId, 
                    date.format(DateTimeFormatter.ISO_DATE));
            redisCacheService.setCache(cacheKey, Integer.valueOf(workload), 30);
        } catch (Exception e) {
            // Redis连接失败，忽略缓存设置
            System.err.println("Redis缓存设置失败: " + e.getMessage());
        }

        return workload;
    }

    @Override
    public int getMemberTotalTasks(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        int total = 0;
        total += safeCount(() -> memberMapper.countDoorCleaningTotalByMember(memberId));
        total += safeCount(() -> memberMapper.countGroomingTotalByMember(memberId));
        total += safeCount(() -> memberMapper.countHospitalTotalByMember(memberId));
        return total;
    }

    /** 安全计数：某表缺 member_id 等异常时返回 0 而非整段失败 */
    private int safeCount(java.util.function.Supplier<Integer> supplier) {
        try {
            Integer v = supplier.get();
            return v != null ? v : 0;
        } catch (Exception e) {
            System.err.println("统计任务数异常（若 grooming/hospital 表缺 member_id 请执行 database/add_member_id_to_grooming_hospital.sql）: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public ServiceMember createMember(ServiceMember member) {
        // 验证必填字段
        if (member.getMemberName() == null || member.getMemberName().trim().isEmpty()) {
            throw new RuntimeException("服务人员姓名不能为空");
        }
        if (member.getServiceType() == null || member.getServiceType().trim().isEmpty()) {
            throw new RuntimeException("服务类型不能为空");
        }
        
        // 如果 userId 为空，设置为 null（允许服务人员没有系统账户）
        if (member.getUserId() == null) {
            member.setUserId(null);
        }
        // 设置默认值
        if (member.getStatus() == null) {
            member.setStatus(1); // 默认启用
        }
        if (member.getMaxTasksPerDay() == null) {
            member.setMaxTasksPerDay(10); // 默认每日最大任务数
        }
        if (member.getRating() == null) {
            member.setRating(java.math.BigDecimal.valueOf(5.0)); // 默认评分
        }
        if (member.getTotalTasks() == null) {
            member.setTotalTasks(0); // 默认任务数为0
        }
        memberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMember(ServiceMember member) {
        if (member.getId() == null) {
            return false;
        }
        boolean ok = memberMapper.updateById(member) > 0;
        if (ok && member.getServiceType() != null && !member.getServiceType().isEmpty()) {
            alignUnbookedSchedulesServiceType(member.getId(), member.getServiceType());
        }
        return ok;
    }

    @Override
    public ServiceMember getMemberById(Long id) {
        return memberMapper.selectById(id);
    }

    @Override
    public ServiceMember getMemberByUserId(Long userId) {
        return memberMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean deleteMember(Long id) {
        // 删除服务人员时，也应该清理相关的预约时间段
        // 但这里为了简化，只删除服务人员记录
        // 如果有关联的预约，可以通过外键约束或手动处理
        return memberMapper.deleteById(id) > 0;
    }

    @Override
    public List<ServiceMember> getAllMembers() {
        // 查询所有成员，不区分服务类型和状态
        return memberMapper.selectAllMembers();
    }

    @Override
    @Transactional
    public ServiceMember syncMemberWithAdmin(Admin admin) {
        if (admin == null) {
            return null;
        }

        String serviceType = convertDepartmentToServiceType(admin.getDepartment());
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return null;
        }

        // 只按 Admin.id (user_id) 匹配，禁止按手机号 fallback，避免不同账号同号时共用一条 ServiceMember 导致互相覆盖 store_id
        ServiceMember existing = null;
        if (admin.getId() != null) {
            existing = memberMapper.selectByUserId(admin.getId());
        }

        if (existing == null) {
            ServiceMember member = new ServiceMember();
            member.setUserId(admin.getId());
            member.setServiceType(serviceType);
            member.setStoreId(admin.getServiceStoreId()); // 仅用服务门店，用于排班/预约过滤；商品店铺(storeId)不写入
            member.setMemberName(admin.getName());
            member.setPhone(admin.getPhone());
            member.setAvatar(admin.getAvatar());
            member.setStatus(admin.getStatus() != null ? admin.getStatus() : 1);
            member.setMaxTasksPerDay(admin.getMaxTasksPerDay() != null ? admin.getMaxTasksPerDay() : 10);
            member.setTotalTasks(0);
            member.setRating(BigDecimal.valueOf(5.0));
            return createMember(member);
        } else {
            existing.setServiceType(serviceType);
            existing.setStoreId(admin.getServiceStoreId());
            existing.setMemberName(admin.getName());
            existing.setPhone(admin.getPhone());
            existing.setAvatar(admin.getAvatar());
            existing.setStatus(admin.getStatus() != null ? admin.getStatus() : existing.getStatus());
            if (admin.getMaxTasksPerDay() != null) {
                existing.setMaxTasksPerDay(admin.getMaxTasksPerDay());
            } else if (existing.getMaxTasksPerDay() == null) {
                existing.setMaxTasksPerDay(10);
            }
            memberMapper.updateById(existing);
            alignUnbookedSchedulesServiceType(existing.getId(), serviceType);
            return existing;
        }
    }

    /**
     * 成员服务类型与后台部门一致后，把未被占用的排班行的 service_type 一并改掉。
     * 表唯一键只有 (member_id, date, time_slot)，不包含类型；历史行可能仍是库默认的 door-cleaning 等，导致洗护页日历为空却报重复插入。
     */
    private void alignUnbookedSchedulesServiceType(Long memberId, String serviceType) {
        if (memberId == null || serviceType == null || serviceType.isEmpty()) {
            return;
        }
        UpdateWrapper<ServiceSchedule> uw = new UpdateWrapper<>();
        uw.eq("member_id", memberId)
                .isNull("task_id")
                .and(w -> w.isNull("reserved_count").or().eq("reserved_count", 0))
                .set("service_type", serviceType);
        scheduleMapper.update(null, uw);
    }

    private String convertDepartmentToServiceType(String department) {
        if (department == null || department.trim().isEmpty()) {
            // 兼容分店管理员未配置部门：给一个可识别类型，确保可生成 service_member 映射用于排班
            return "store_admin";
        }
        switch (department.trim()) {
            case "上门铲屎":
            case "上门铲屎部门":
                return "door-cleaning";
            case "宠物洗护":
            case "宠物洗护部门":
                return "grooming";
            case "宠物医院":
            case "宠物医院部门":
                return "hospital";
            case "分店管理员":
                return "store_admin";
            default:
                return department.trim();
        }
    }
}

