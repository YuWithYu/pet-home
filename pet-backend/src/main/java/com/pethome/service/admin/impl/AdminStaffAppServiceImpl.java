package com.pethome.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.AdminRole;
import com.pethome.common.BusinessException;
import com.pethome.common.ServiceTypeCatalog;
import com.pethome.entity.Admin;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.ServiceMember;
import com.pethome.service.admin.AdminService;
import com.pethome.service.admin.AdminStaffAppService;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import com.pethome.service.appointment.GroomingAppointmentService;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.pethome.service.appointment.ServiceAppointmentRatingService;
import com.pethome.service.appointment.ServiceMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminStaffAppServiceImpl implements AdminStaffAppService {

    private static final Logger log = LoggerFactory.getLogger(AdminStaffAppServiceImpl.class);

    private static final String DEPARTMENT_HOSPITAL = "宠物医院部门";
    private static final String DEPARTMENT_HOSPITAL_ALIAS = "宠物医疗";

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServiceMemberService serviceMemberService;

    @Autowired(required = false)
    private ServiceAppointmentRatingService serviceAppointmentRatingService;

    @Autowired(required = false)
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    @Autowired(required = false)
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired(required = false)
    private HospitalAppointmentService hospitalAppointmentService;

    @Override
    public List<Admin> listStaff(String department, Long storeId, Boolean accountsOnly, Long currentAdminId) {
        Admin currentAdmin = currentAdminId != null ? adminService.getById(currentAdminId) : null;
        String deptFilter = department != null ? department.trim() : null;
        if (deptFilter != null && deptFilter.isEmpty()) {
            deptFilter = null;
        }

        // 门店管理员（有 serviceStoreId 且非平台管理员）：强制只看本门店员工，防止越权
        Long effectiveStoreId = storeId;
        if (currentAdmin != null && currentAdmin.getServiceStoreId() != null
                && !AdminRole.isAdmin(currentAdmin.getRole())) {
            effectiveStoreId = currentAdmin.getServiceStoreId();
        }

        List<Admin> staffList;
        if (Boolean.TRUE.equals(accountsOnly)) {
            // 账号管理页返回真实 Admin 且包含禁用账号
            staffList = listWithDisabled(effectiveStoreId);
        } else if (currentAdmin != null && AdminRole.isStaff(currentAdmin.getRole())) {
            // 服务人员仅能看到自己
            staffList = new ArrayList<>(java.util.Collections.singletonList(currentAdmin));
        } else {
            staffList = effectiveStoreId != null
                    ? adminService.listStaffByStoreId(effectiveStoreId)
                    : adminService.listAllStaff();
        }

        if (deptFilter != null) {
            staffList = filterByDepartment(staffList, deptFilter, effectiveStoreId);
        }

        staffList.forEach(staff -> staff.setPassword(null));

        // accountsOnly=true 时不做 ServiceMember 回退，避免出现「删不掉的幽灵账号」
        boolean usedFallback = false;
        if (staffList.isEmpty() && effectiveStoreId == null && !Boolean.TRUE.equals(accountsOnly)) {
            usedFallback = true;
            staffList = fallbackFromServiceMembers();
        }

        if (!usedFallback) {
            enrichStaffRuntimeStats(staffList);
        }

        // 统一展示名：宠物医疗 -> 宠物医院部门
        staffList.forEach(staff -> {
            if (DEPARTMENT_HOSPITAL_ALIAS.equals(staff.getDepartment())) {
                staff.setDepartment(DEPARTMENT_HOSPITAL);
            }
        });
        return staffList;
    }

    private List<Admin> listWithDisabled(Long storeId) {
        return storeId != null
                ? adminService.listStaffByStoreIdWithDisabled(storeId)
                : adminService.listAllStaffWithDisabled();
    }

    /**
     * 按部门筛选。
     * 「宠物医院」存在两个历史部门名（宠物医院部门 / 宠物医疗），需要合并展示；
     * 门店范围内只能在已收敛的列表里过滤，避免把其他门店员工带出来。
     */
    private List<Admin> filterByDepartment(List<Admin> source, String deptFilter, Long storeId) {
        boolean hospitalFilter = DEPARTMENT_HOSPITAL.equals(deptFilter) || "宠物医院".equals(deptFilter)
                || DEPARTMENT_HOSPITAL_ALIAS.equals(deptFilter);
        if (!hospitalFilter) {
            return source.stream().filter(s -> deptFilter.equals(s.getDepartment())).collect(Collectors.toList());
        }
        if (storeId != null) {
            return source.stream()
                    .filter(s -> DEPARTMENT_HOSPITAL.equals(s.getDepartment())
                            || DEPARTMENT_HOSPITAL_ALIAS.equals(s.getDepartment()))
                    .collect(Collectors.toList());
        }
        Set<Long> ids = new HashSet<>();
        List<Admin> merged = new ArrayList<>();
        for (Admin admin : source) {
            if (admin.getId() != null && ids.add(admin.getId())) {
                merged.add(admin);
            }
        }
        for (Admin admin : adminService.listStaffByDepartment(DEPARTMENT_HOSPITAL)) {
            if (admin.getId() != null && ids.add(admin.getId())) {
                merged.add(admin);
            }
        }
        for (Admin admin : adminService.listStaffByDepartment(DEPARTMENT_HOSPITAL_ALIAS)) {
            if (admin.getId() != null && ids.add(admin.getId())) {
                merged.add(admin);
            }
        }
        return merged;
    }

    /** 团队管理页展示回退：用服务成员数据补齐展示信息（其 id 并非 Admin.id） */
    private List<Admin> fallbackFromServiceMembers() {
        List<ServiceMember> members = serviceMemberService.getAllMembers();
        List<Admin> staffList = new ArrayList<>();
        for (ServiceMember member : members) {
            Admin staff = new Admin();
            staff.setId(member.getId());
            staff.setName(member.getMemberName());
            staff.setDepartment(ServiceTypeCatalog.departmentName(member.getServiceType()));
            staff.setPhone(member.getPhone());
            staff.setAvatar(member.getAvatar());
            staff.setRole(AdminRole.STAFF);
            staff.setStatus(member.getStatus() != null ? member.getStatus() : 1);
            staffList.add(staff);
        }
        return staffList;
    }

    /**
     * 补齐员工展示用的评分/总任务数/今日工作量。
     * 评分与任务数改为批量 GROUP BY 聚合，替代逐人多次查询的 N+1。
     */
    private void enrichStaffRuntimeStats(List<Admin> staffList) {
        Map<Long, ServiceMember> memberByAdminId = new HashMap<>();
        for (Admin staff : staffList) {
            if (staff.getId() == null || !AdminRole.STAFF.equals(staff.getRole())) {
                continue;
            }
            try {
                ServiceMember member = serviceMemberService.syncMemberWithAdmin(staff);
                if (member != null) {
                    memberByAdminId.put(staff.getId(), member);
                }
            } catch (Exception e) {
                log.warn("同步服务成员失败: adminId={}", staff.getId(), e);
            }
        }
        if (memberByAdminId.isEmpty()) {
            return;
        }
        List<Long> memberIds = new ArrayList<>();
        for (ServiceMember member : memberByAdminId.values()) {
            memberIds.add(member.getId());
        }
        Map<Long, BigDecimal> ratingByMember = batchAvgRating(memberIds);
        Map<Long, Integer> totalTasksByMember = batchCountTotalTasks(memberIds);
        Map<Long, Integer> workloadByMember = batchCountTodayWorkload(memberIds, LocalDate.now());
        for (Admin staff : staffList) {
            ServiceMember member = memberByAdminId.get(staff.getId());
            if (member == null) {
                continue;
            }
            // 优先使用评价表的真实平均分，避免显示过期的 ServiceMember.rating
            BigDecimal avgFromRatings = ratingByMember.get(member.getId());
            staff.setRating(avgFromRatings != null ? avgFromRatings : member.getRating());
            staff.setTotalTasks(totalTasksByMember.getOrDefault(member.getId(), 0));
            staff.setMaxTasksPerDay(member.getMaxTasksPerDay() != null ? member.getMaxTasksPerDay() : 10);
            staff.setServiceMemberId(member.getId());
            staff.setTodayWorkload(workloadByMember.getOrDefault(member.getId(), 0));
        }
    }

    private Map<Long, BigDecimal> batchAvgRating(List<Long> memberIds) {
        if (serviceAppointmentRatingService == null || memberIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return serviceAppointmentRatingService.avgRatingByMemberIds(memberIds);
        } catch (Exception e) {
            log.warn("批量查询成员评分失败", e);
            return new HashMap<>();
        }
    }

    /** 批量统计成员总任务数（三张预约表各一条 GROUP BY，过滤 status != cancelled） */
    private Map<Long, Integer> batchCountTotalTasks(List<Long> memberIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (memberIds.isEmpty()) {
            return result;
        }
        if (doorCleaningAppointmentService != null) {
            QueryWrapper<DoorCleaningAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .ne("status", "cancelled")
                    .groupBy("member_id");
            mergeCountMap(result, doorCleaningAppointmentService.listMaps(w));
        }
        if (groomingAppointmentService != null) {
            QueryWrapper<GroomingAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .ne("status", "cancelled")
                    .groupBy("member_id");
            mergeCountMap(result, groomingAppointmentService.listMaps(w));
        }
        if (hospitalAppointmentService != null) {
            QueryWrapper<HospitalAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .ne("status", "cancelled")
                    .groupBy("member_id");
            mergeCountMap(result, hospitalAppointmentService.listMaps(w));
        }
        return result;
    }

    /** 批量统计成员今日工作量（当日 + pending/confirmed） */
    private Map<Long, Integer> batchCountTodayWorkload(List<Long> memberIds, LocalDate date) {
        Map<Long, Integer> result = new HashMap<>();
        if (memberIds.isEmpty() || date == null) {
            return result;
        }
        String dateStr = date.toString();
        if (doorCleaningAppointmentService != null) {
            QueryWrapper<DoorCleaningAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .apply("DATE(COALESCE(appointment_date, date)) = {0}", dateStr)
                    .in("status", "pending", "confirmed")
                    .groupBy("member_id");
            mergeCountMap(result, doorCleaningAppointmentService.listMaps(w));
        }
        if (groomingAppointmentService != null) {
            QueryWrapper<GroomingAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .eq("date", dateStr)
                    .in("status", "pending", "confirmed")
                    .groupBy("member_id");
            mergeCountMap(result, groomingAppointmentService.listMaps(w));
        }
        if (hospitalAppointmentService != null) {
            QueryWrapper<HospitalAppointment> w = new QueryWrapper<>();
            w.select("member_id", "COUNT(*) AS cnt")
                    .in("member_id", memberIds)
                    .eq("date", dateStr)
                    .in("status", "pending", "confirmed")
                    .groupBy("member_id");
            mergeCountMap(result, hospitalAppointmentService.listMaps(w));
        }
        return result;
    }

    private void mergeCountMap(Map<Long, Integer> target, List<Map<String, Object>> rows) {
        if (rows == null) {
            return;
        }
        for (Map<String, Object> row : rows) {
            Object memberId = row.get("member_id");
            Object cnt = row.get("cnt");
            if (memberId instanceof Number && cnt instanceof Number) {
                target.merge(((Number) memberId).longValue(), ((Number) cnt).intValue(), Integer::sum);
            }
        }
    }

    @Override
    public Admin addStaff(Admin staff, String currentRole) {
        if (!AdminRole.isAdmin(currentRole)) {
            throw new BusinessException("仅管理员可在权限管理中创建新账号");
        }
        if (staff == null) {
            throw new BusinessException("账号信息不能为空");
        }
        if (staff.getUsername() == null || staff.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (staff.getPassword() == null || staff.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (staff.getName() == null || staff.getName().trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        String role = AdminRole.normalize(staff.getRole());
        staff.setRole(role);
        staff.setDoctorId(null);
        validateStoreBinding(staff, role);

        Admin created;
        try {
            created = adminService.createAdmin(staff);
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("用户名已存在")) {
                throw new BusinessException("用户名已存在，请使用其他用户名");
            }
            throw e;
        }
        created.setPassword(null);
        if (AdminRole.STAFF.equals(created.getRole())) {
            serviceMemberService.syncMemberWithAdmin(adminService.getById(created.getId()));
        }
        return created;
    }

    @Override
    public boolean updateStaff(Admin staff, Long currentAdminId) {
        if (staff == null || staff.getId() == null) {
            throw new BusinessException("账号信息不能为空");
        }
        String role = AdminRole.normalize(staff.getRole());
        // 自我保护：不允许把自己的账号降级为非 admin
        if (currentAdminId != null && currentAdminId.equals(staff.getId()) && !AdminRole.ADMIN.equals(role)) {
            throw new BusinessException("不能降级当前登录账号的角色");
        }
        // 唯一管理员保护：把任意 admin 降级前，确认系统中还有其他可用 admin
        Admin target = adminService.getById(staff.getId());
        if (target != null && AdminRole.isAdmin(target.getRole()) && !AdminRole.ADMIN.equals(role)) {
            requireAnotherActiveAdmin("系统仅剩一个管理员账号，不能降级");
        }
        staff.setRole(role);
        staff.setDoctorId(null);
        validateStoreBinding(staff, role);
        if (DEPARTMENT_HOSPITAL_ALIAS.equals(staff.getDepartment())) {
            staff.setDepartment(DEPARTMENT_HOSPITAL);
        }

        boolean success = adminService.updateAdmin(staff);
        if (success && AdminRole.STAFF.equals(staff.getRole())) {
            Admin dbAdmin = adminService.getById(staff.getId());
            if (dbAdmin != null) {
                if (staff.getMaxTasksPerDay() != null) {
                    dbAdmin.setMaxTasksPerDay(staff.getMaxTasksPerDay());
                }
                serviceMemberService.syncMemberWithAdmin(dbAdmin);
            }
        }
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteStaff(Long id, Long currentAdminId) {
        if (currentAdminId != null && currentAdminId.equals(id)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        Admin admin = adminService.getById(id);
        if (admin == null) {
            // 团队管理页回退展示的 ServiceMember（id 为 ServiceMember.id）
            if (hasPendingAppointmentsByMemberId(id)) {
                throw new BusinessException("该成员存在待处理/已确认的预约，请先处理或转移后再删除");
            }
            serviceMemberService.deleteMember(id);
            return true;
        }
        if (AdminRole.isAdmin(admin.getRole())) {
            if ("admin".equalsIgnoreCase(admin.getUsername())) {
                throw new BusinessException("不能删除默认管理员账号");
            }
            requireAnotherActiveAdmin("系统仅剩一个管理员账号，不能删除");
        } else if (AdminRole.isStaff(admin.getRole())) {
            // 删除服务人员前校验其名下是否还有未完成预约，防止悬空引用
            ServiceMember member = serviceMemberService.getMemberByUserId(admin.getId());
            if (member != null && hasPendingAppointmentsByMemberId(member.getId())) {
                throw new BusinessException("该成员存在待处理/已确认的预约，请先处理或转移后再删除");
            }
        }
        if (!adminService.deleteAdmin(id)) {
            throw new BusinessException("删除失败");
        }
        return true;
    }

    @Override
    public Admin getStaffById(Long id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            throw new BusinessException("员工不存在");
        }
        admin.setPassword(null);
        if (admin.isStaff()) {
            ServiceMember member = serviceMemberService.getMemberByUserId(admin.getId());
            if (member != null) {
                if (serviceAppointmentRatingService != null) {
                    BigDecimal avgFromRatings = serviceAppointmentRatingService.avgRatingByMemberId(member.getId());
                    admin.setRating(avgFromRatings != null ? avgFromRatings : member.getRating());
                } else {
                    admin.setRating(member.getRating());
                }
                admin.setTotalTasks(member.getTotalTasks());
                admin.setMaxTasksPerDay(member.getMaxTasksPerDay());
                admin.setServiceMemberId(member.getId());
                admin.setTodayWorkload(serviceMemberService.getMemberWorkload(member.getId(), LocalDate.now()));
            }
        }
        return admin;
    }

    private void validateStoreBinding(Admin staff, String role) {
        if (AdminRole.ADMIN.equals(role)) {
            staff.setServiceStoreId(null);
        } else if (AdminRole.STORE_ADMIN.equals(role) && staff.getServiceStoreId() == null) {
            throw new BusinessException("分店管理员必须绑定服务门店");
        }
    }

    private void requireAnotherActiveAdmin(String message) {
        long activeAdmins = adminService.listAllStaffWithDisabled().stream()
                .filter(a -> AdminRole.isAdmin(a.getRole())
                        && !"disabled".equalsIgnoreCase(String.valueOf(a.getStatus())))
                .count();
        if (activeAdmins <= 1) {
            throw new BusinessException(message);
        }
    }

    /** 校验服务人员名下是否存在未完成（待处理/已确认）预约 */
    private boolean hasPendingAppointmentsByMemberId(Long memberId) {
        try {
            if (doorCleaningAppointmentService != null
                    && doorCleaningAppointmentService.getAppointmentsByMemberId(memberId).stream()
                    .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()))) {
                return true;
            }
            if (groomingAppointmentService != null
                    && groomingAppointmentService.getAppointmentsByMemberId(memberId).stream()
                    .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()))) {
                return true;
            }
            if (hospitalAppointmentService != null
                    && hospitalAppointmentService.getAppointmentsByMemberId(memberId).stream()
                    .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()))) {
                return true;
            }
        } catch (Exception e) {
            log.warn("校验成员未完成预约失败: memberId={}", memberId, e);
        }
        return false;
    }
}
