package com.pethome.controller.admin;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.Complaint;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.User;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.service.user.UserService;
import com.pethome.service.user.PetService;
import com.pethome.security.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 管理员控制器
 * 用于管理员登录和团队管理
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员接口")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    /** 管理员登录防爆破：失败次数 key 前缀 */
    private static final String ADMIN_LOGIN_FAIL_PREFIX = "admin:login:fail:";
    /** 连续失败锁定阈值 */
    private static final int MAX_LOGIN_FAILS = 5;
    /** 锁定时长（分钟） */
    private static final long LOGIN_LOCK_MINUTES = 15;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private String normalizeRole(String role) {
        if (role == null) return "staff";
        String r = role.trim().toLowerCase();
        if ("super_admin".equals(r)) return "admin";
        if (!"admin".equals(r) && !"staff".equals(r) && !"store_admin".equals(r)) return "staff";
        return r;
    }

    /** 记录登录失败次数（15 分钟窗口，达到阈值后锁定） */
    private void recordLoginFailure(String failKey, int currentFails) {
        try {
            stringRedisTemplate.opsForValue().set(failKey, String.valueOf(currentFails + 1),
                    LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: key={}", failKey, e);
        }
    }
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ServiceMemberService serviceMemberService;

    @Autowired
    private PetService petService;

    @Autowired(required = false)
    private com.pethome.service.appointment.ServiceAppointmentRatingService serviceAppointmentRatingService;
    @Autowired(required = false)
    private com.pethome.service.appointment.DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired(required = false)
    private com.pethome.service.appointment.GroomingAppointmentService groomingAppointmentService;
    @Autowired(required = false)
    private com.pethome.service.appointment.HospitalAppointmentService hospitalAppointmentService;
    
    @Autowired(required = false)
    private com.pethome.service.admin.AdminPermissionService adminPermissionService;
    
    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;
    
    @Autowired(required = false)
    private com.pethome.service.community.ComplaintService complaintService;
    
    @Autowired(required = false)
    private UserService userService;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        try {
            String password = request.get("password");
            
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 安全修复：登录防爆破——同一账号连续失败 5 次锁定 15 分钟
            String failKey = ADMIN_LOGIN_FAIL_PREFIX + username.trim();
            String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
            int failCount = 0;
            try {
                failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
            } catch (NumberFormatException ignored) {
            }
            if (failCount >= MAX_LOGIN_FAILS) {
                log.warn("管理员登录被锁定（连续失败 {} 次）: username={}", failCount, username);
                return Result.error("登录失败次数过多，账号已锁定 15 分钟，请稍后再试");
            }

            // 查询管理员（兼容历史 super_admin 账号可用 admin 登录）
            Admin admin = adminService.getByUsername(username.trim());
            if (admin == null && "admin".equalsIgnoreCase(username.trim())) {
                admin = adminService.getByUsername("super_admin");
            }
            if (admin == null) {
                recordLoginFailure(failKey, failCount);
                return Result.error("账号或密码错误");
            }

            // 验证账号状态
            if (!admin.isEnabled()) {
                return Result.error("账号已被禁用");
            }

            // 验证密码
            if (!adminService.verifyPassword(password, admin.getPassword())) {
                recordLoginFailure(failKey, failCount);
                return Result.error("账号或密码错误");
            }

            // 登录成功：清除失败计数
            stringRedisTemplate.delete(failKey);

            // 安全修复：历史明文密码在首次登录成功后自动升级为 BCrypt（与用户端 MD5→BCrypt 迁移策略一致）
            String storedPassword = admin.getPassword();
            if (storedPassword != null && !storedPassword.startsWith("$2a$")) {
                try {
                    admin.setPassword(adminService.encodePassword(password));
                    adminService.updateAdmin(admin);
                    log.info("管理员明文密码已自动升级为 BCrypt: username={}", username);
                } catch (Exception e) {
                    // 升级失败不影响本次登录，但必须留痕
                    log.warn("管理员密码 BCrypt 升级失败: username={}", username, e);
                }
            }
            
            // 获取部门ID（如果department字段存储的是部门名称，需要通过名称查询ID）
            Long departmentId = null;
            if (admin.getDepartment() != null && !admin.getDepartment().isEmpty()) {
                // 这里需要注入DepartmentService来查询部门ID
                // 暂时先返回null，后续可以从token中解析department名称后再查询
                // departmentId = departmentService.getDepartmentIdByName(admin.getDepartment());
            }
            
            // 生成JWT Token（包含用户名、角色、部门ID）
            String role = normalizeRole(admin.getRole());
            if (!role.equals(admin.getRole())) {
                admin.setRole(role);
                adminService.updateAdmin(admin);
            }
            String token = jwtUtil.generateToken(admin.getUsername(), role, departmentId);
            
            // 返回登录信息
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("token", token);
            loginInfo.put("adminId", admin.getId());
            loginInfo.put("username", admin.getUsername());
            loginInfo.put("name", admin.getName());
            loginInfo.put("role", role);
            loginInfo.put("department", admin.getDepartment());
            loginInfo.put("departmentId", departmentId);
            loginInfo.put("storeId", admin.getStoreId());
            loginInfo.put("serviceStoreId", admin.getServiceStoreId()); // 服务门店，用于排班/预约过滤
            loginInfo.put("avatar", admin.getAvatar());
            
            return Result.success(loginInfo);
        } catch (IllegalArgumentException e) {
            // 参数/业务校验失败：返回具体提示
            log.error("管理员登录失败", e);
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            // 其他异常：不向前端泄露内部细节，仅记录日志
            log.error("管理员登录失败: username={}", username, e);
            return Result.error(500, "登录失败，请稍后重试");
        }
    }
    
    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前登录管理员信息")
    public Result<Map<String, Object>> getCurrentAdmin(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            
            token = token.substring(7);
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                if (username == null) {
                    return Result.error("Token无效或已过期");
                }
                
                Admin admin = adminService.getByUsername(username);
                if (admin == null) {
                    return Result.error("管理员不存在");
                }
                
                // 不返回密码
                admin.setPassword(null);
                
                // 构建返回数据
                Map<String, Object> result = new HashMap<>();
                result.put("id", admin.getId());
                result.put("username", admin.getUsername());
                result.put("name", admin.getName());
                result.put("role", normalizeRole(admin.getRole()));
                result.put("department", admin.getDepartment());
                result.put("storeId", admin.getStoreId());
                result.put("serviceStoreId", admin.getServiceStoreId());
                result.put("phone", admin.getPhone());
                result.put("email", admin.getEmail());
                result.put("avatar", admin.getAvatar());
                result.put("status", admin.getStatus());
                
                // 获取权限列表
                List<String> permissionCodes = new ArrayList<>();
                try {
                    // 首先尝试从数据库获取权限（无论角色是什么，如果设置了权限就使用）
                    if (adminPermissionService != null) {
                        try {
                            List<com.pethome.entity.AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(admin.getId());

                            if (permissions != null && !permissions.isEmpty()) {
                                permissionCodes = permissions.stream()
                                    .filter(p -> p != null && p.getStatus() != null && p.getStatus() == 1)
                                    .map(com.pethome.entity.AdminPermission::getPermissionCode)
                                    .filter(code -> code != null && !code.trim().isEmpty())
                                    .collect(java.util.stream.Collectors.toList());
                            }
                        } catch (Exception e) {
                            log.warn("查询权限时出错: adminId={}", admin.getId(), e);
                        }
                    } else {
                        log.warn("adminPermissionService 未注入，无法查询权限");
                    }

                    // 如果权限表中没有记录，根据角色决定默认权限
                    if (permissionCodes.isEmpty()) {
                        String normalizedRole = normalizeRole(admin.getRole());
                        if ("admin".equals(normalizedRole)) {
                            // 管理员角色，如果没有设置权限，默认拥有所有权限（兼容旧系统）
                            // 但建议通过权限设置来限制
                            permissionCodes.add("admin");
                        } else if ("store_admin".equals(normalizedRole)) {
                            // 分店管理员默认给一组可用菜单权限（数据范围仍由 serviceStoreId 在后端强制约束）
                            permissionCodes.add("/service-staff");
                            permissionCodes.add("/my-schedule");
                            permissionCodes.add("/service-orders/appointment-orders");
                            permissionCodes.add("/verify");
                            permissionCodes.add("/outlet-customer-chat");
                            permissionCodes.add("/users/permissions");
                        }
                        // 员工等角色：未在权限管理中设置则不自动分配，由管理员自行在权限设置中分配
                    }
                    // 重要：如果权限表中有记录，就使用权限表中的权限（即使角色是super_admin或admin）
                    // 这样可以限制任何账号的访问权限
                    // 门店管理员自动拥有门店客服权限（把门店管理员当成客服）
                    if (admin.getServiceStoreId() != null && !permissionCodes.contains("/services/outlet-customer-chat")) {
                        permissionCodes.add("/services/outlet-customer-chat");
                    }
                    // 店铺管理员自动拥有店铺客服权限
                    if (admin.getStoreId() != null && !permissionCodes.contains("/mall/store-customer-chat")) {
                        permissionCodes.add("/mall/store-customer-chat");
                    }
                    if ("admin".equals(normalizeRole(admin.getRole())) && permissionCodes.contains("/my-schedule")) {
                        permissionCodes.remove("/my-schedule");
                    }
                } catch (Exception permError) {
                    // 如果权限表不存在或其他错误，根据角色使用默认权限
                    log.warn("获取权限失败（可能是表不存在）: adminId={}", admin.getId(), permError);
                    if ("admin".equals(normalizeRole(admin.getRole()))) {
                        permissionCodes.add("admin");
                    }
                    // 员工等角色不自动加 admin，需在权限管理中手动分配
                }
                result.put("permissions", permissionCodes);

                log.debug("管理员权限获取完成: adminId={}, permissionCount={}", admin.getId(), permissionCodes.size());

                return Result.success(result);
            } catch (Exception tokenError) {
                log.warn("解析 Token 失败", tokenError);
                return Result.error(401, "Token无效或已过期");
            }
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return Result.error(500, "获取管理员信息失败，请稍后重试");
        }
    }
    
    /**
     * 获取员工列表（根据部门过滤，包括商家账号）
     */
    @GetMapping({"/staff/list", "/listStaff"})
    @Operation(summary = "获取员工列表（包括商家账号，可选按门店筛选）")
    public Result<List<Admin>> listStaff(@RequestParam(required = false) String department,
                                          @RequestParam(required = false) Long storeId,
                                          @RequestParam(required = false) Boolean accountsOnly,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);

            List<Admin> staffList;
            String deptFilter = department != null ? department.trim() : null;
            
            // 门店管理员（有 serviceStoreId 且非超级管理员）：强制只看本门店员工，防止越权
            Long effectiveStoreId = storeId;
            if (currentAdmin != null && currentAdmin.getServiceStoreId() != null 
                    && !"admin".equals(normalizeRole(currentAdmin.getRole()))) {
                effectiveStoreId = currentAdmin.getServiceStoreId();
            }

            // accountsOnly=true（权限管理页）时返回真实 Admin 且包含禁用账号
            if (Boolean.TRUE.equals(accountsOnly)) {
                if (effectiveStoreId != null) {
                    staffList = adminService.listStaffByStoreIdWithDisabled(effectiveStoreId);
                    if (deptFilter != null && !deptFilter.isEmpty()) {
                        final String df = deptFilter;
                        staffList = staffList.stream()
                            .filter(s -> df.equals(s.getDepartment())
                                || ("宠物医疗".equals(s.getDepartment()) && ("宠物医院".equals(df) || "宠物医院部门".equals(df))))
                            .collect(java.util.stream.Collectors.toList());
                    }
                } else if (deptFilter != null && !deptFilter.isEmpty()) {
                    // 账号管理页对部门做前端过滤，避免新增过多 service 接口
                    List<Admin> allWithDisabled = adminService.listAllStaffWithDisabled();
                    final String df = deptFilter;
                    staffList = allWithDisabled.stream()
                        .filter(s -> df.equals(s.getDepartment())
                            || ("宠物医疗".equals(s.getDepartment()) && ("宠物医院".equals(df) || "宠物医院部门".equals(df))))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    staffList = adminService.listAllStaffWithDisabled();
                }
            } else if (currentAdmin != null && "staff".equals(normalizeRole(currentAdmin.getRole()))) {
                // 服务人员仅能看到自己
                staffList = java.util.Collections.singletonList(currentAdmin);
            } else {
                if (effectiveStoreId != null) {
                    staffList = adminService.listStaffByStoreId(effectiveStoreId);
                    if (deptFilter != null && !deptFilter.isEmpty()) {
                        final String df = deptFilter;
                        staffList = staffList.stream()
                            .filter(s -> df.equals(s.getDepartment())
                                || ("宠物医疗".equals(s.getDepartment()) && ("宠物医院".equals(df) || "宠物医院部门".equals(df))))
                            .collect(java.util.stream.Collectors.toList());
                    }
                } else if (deptFilter != null && !deptFilter.isEmpty()) {
                    if ("宠物医院".equals(deptFilter) || "宠物医院部门".equals(deptFilter)) {
                        List<Admin> byDept = adminService.listStaffByDepartment("宠物医院部门");
                        List<Admin> byAlias = adminService.listStaffByDepartment("宠物医疗");
                        java.util.Set<Long> ids = new java.util.HashSet<>();
                        staffList = new java.util.ArrayList<>();
                        for (Admin a : byDept) { if (a.getId() != null && ids.add(a.getId())) staffList.add(a); }
                        for (Admin a : byAlias) { if (a.getId() != null && ids.add(a.getId())) staffList.add(a); }
                    } else {
                        staffList = adminService.listStaffByDepartment(deptFilter);
                    }
                } else {
                    staffList = adminService.listAllStaff();
                }
            }
            
            // 清除密码字段
            staffList.forEach(staff -> staff.setPassword(null));
            
            // accountsOnly=true 时（账号管理页）不做 ServiceMember 回退，只返回真实 Admin，否则会出现「删不掉的幽灵账号」
            // 仅当查看「全部门店」且无数据且非 accountsOnly 时，用 ServiceMember 做展示回退
            boolean usedFallback = false;
            if (staffList.isEmpty() && effectiveStoreId == null && !Boolean.TRUE.equals(accountsOnly)) {
                usedFallback = true;
                List<ServiceMember> serviceMembers = serviceMemberService.getAllMembers();
                staffList = serviceMembers.stream().map(member -> {
                    Admin staff = new Admin();
                    staff.setId(member.getId());
                    staff.setName(member.getMemberName());
                    staff.setDepartment(resolveDepartmentName(member.getServiceType()));
                    staff.setPhone(member.getPhone());
                    staff.setAvatar(member.getAvatar());
                    staff.setRole("staff");
                    staff.setStatus(member.getStatus() != null ? member.getStatus() : 1);
                    return staff;
                }).collect(java.util.stream.Collectors.toList());
            }

            // 仅对真实 Admin（来自数据库）做 sync 和统计合并；fallback 展示的 ServiceMember 映射跳过（其 id 并非 Admin.id）
            // 性能：sync 返回值即该账号的 ServiceMember（无需再逐个 getById + getMemberByUserId 重复查询），
            // 评分/总任务数/今日工作量改为批量 GROUP BY 聚合，替代逐人多次查询的 N+1
            if (!usedFallback) {
                Map<Long, ServiceMember> memberByAdminId = new HashMap<>();
                for (Admin staff : staffList) {
                    if (staff.getId() == null || !"staff".equals(staff.getRole())) {
                        continue;
                    }
                    try {
                        ServiceMember sm = serviceMemberService.syncMemberWithAdmin(staff);
                        if (sm != null) {
                            memberByAdminId.put(staff.getId(), sm);
                        }
                    } catch (Exception ignore) {
                    }
                }
                if (!memberByAdminId.isEmpty()) {
                    List<Long> memberIds = new ArrayList<>();
                    for (ServiceMember sm : memberByAdminId.values()) {
                        memberIds.add(sm.getId());
                    }
                    Map<Long, BigDecimal> ratingByMember = batchAvgRating(memberIds);
                    Map<Long, Integer> totalTasksByMember = batchCountTotalTasks(memberIds);
                    Map<Long, Integer> workloadByMember = batchCountTodayWorkload(memberIds, LocalDate.now());
                    for (Admin staff : staffList) {
                        ServiceMember sm = memberByAdminId.get(staff.getId());
                        if (sm != null) {
                            // 优先使用评价表的真实平均分，避免显示过期的 ServiceMember.rating
                            BigDecimal avgFromRatings = ratingByMember.get(sm.getId());
                            staff.setRating(avgFromRatings != null ? avgFromRatings : sm.getRating());
                            // 总任务数：动态统计各预约表中的任务数，不使用静态字段
                            staff.setTotalTasks(totalTasksByMember.getOrDefault(sm.getId(), 0));
                            staff.setMaxTasksPerDay(sm.getMaxTasksPerDay() != null ? sm.getMaxTasksPerDay() : 10);
                            staff.setServiceMemberId(sm.getId());
                            staff.setTodayWorkload(workloadByMember.getOrDefault(sm.getId(), 0));
                        }
                    }
                }
            }
            
            // 统一展示名：宠物医疗 -> 宠物医院部门，避免与「宠物医院」混淆
            for (Admin staff : staffList) {
                if ("宠物医疗".equals(staff.getDepartment())) {
                    staff.setDepartment("宠物医院部门");
                }
            }
            
            return Result.success(staffList);
        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            return Result.error("查询员工列表失败，请稍后重试");
        }
    }

    /** 批量查询成员平均评分（一条 GROUP BY 替代逐人 AVG 查询） */
    private Map<Long, BigDecimal> batchAvgRating(List<Long> memberIds) {
        if (serviceAppointmentRatingService == null || memberIds == null || memberIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return serviceAppointmentRatingService.avgRatingByMemberIds(memberIds);
        } catch (Exception e) {
            log.warn("批量查询成员评分失败", e);
            return new HashMap<>();
        }
    }

    /** 批量统计成员总任务数（三张预约表各一条 GROUP BY，过滤条件与单查版一致：status != 'cancelled'） */
    private Map<Long, Integer> batchCountTotalTasks(List<Long> memberIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (memberIds == null || memberIds.isEmpty()) {
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

    /** 批量统计成员今日工作量（三张预约表各一条 GROUP BY，过滤条件与单查版一致：当日 + pending/confirmed） */
    private Map<Long, Integer> batchCountTodayWorkload(List<Long> memberIds, LocalDate date) {
        Map<Long, Integer> result = new HashMap<>();
        if (memberIds == null || memberIds.isEmpty() || date == null) {
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
    
    /**
     * 从 token 获取当前管理员信息（内部方法）
     */
    private Admin getCurrentAdminFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return null;
            }
            return adminService.getByUsername(username);
        } catch (Exception e) {
            log.warn("解析 token 失败", e);
            return null;
        }
    }
    
    /**
     * 当前登录用户更新头像（任意后台用户均可调用，只更新自己的头像，全系统同步）
     */
    @PutMapping("/profile/avatar")
    @Operation(summary = "当前用户更新头像")
    public Result<Map<String, Object>> updateMyAvatar(@RequestBody Map<String, String> body,
                                                       @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin admin = getCurrentAdminFromToken(token);
            if (admin == null) {
                return Result.error("请先登录");
            }
            String avatar = body != null ? body.get("avatar") : null;
            if (avatar == null || avatar.trim().isEmpty()) {
                return Result.error("头像地址不能为空");
            }
            avatar = avatar.trim();
            // 存库使用相对路径，便于多环境一致
            if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                int pathStart = avatar.indexOf("/", avatar.indexOf("://") + 3);
                if (pathStart > 0) {
                    avatar = avatar.substring(pathStart);
                    if (!avatar.startsWith("/")) avatar = "/" + avatar;
                }
            }
            admin.setAvatar(avatar);
            boolean ok = adminService.updateAdmin(admin);
            if (!ok) {
                return Result.error("更新失败");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("avatar", admin.getAvatar());
            return Result.success(result);
        }, "更新头像失败，请稍后重试", log);
    }
    
    /**
     * 添加员工或商家账号
     */
    @PostMapping("/staff/add")
    @Operation(summary = "添加员工或商家账号（仅超级管理员可操作）")
    public Result<Admin> addStaff(@RequestBody Admin staff, javax.servlet.http.HttpServletRequest request) {
        try {
            // 仅管理员可创建新账号
            String currentRole = (String) request.getAttribute("role");
            if (currentRole == null || !"admin".equalsIgnoreCase(normalizeRole(currentRole))) {
                return Result.error("仅管理员可在权限管理中创建新账号");
            }
            // 验证必填字段
            if (staff.getUsername() == null || staff.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (staff.getPassword() == null || staff.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (staff.getName() == null || staff.getName().trim().isEmpty()) {
                return Result.error("姓名不能为空");
            }
            
            // 如果角色为空，默认为员工
            if (staff.getRole() == null || staff.getRole().isEmpty()) {
                staff.setRole("staff");
            }
            
            String role = normalizeRole(staff.getRole());
            if (!"admin".equals(role) && !"staff".equals(role) && !"store_admin".equals(role)) {
                return Result.error("角色只能是：admin、store_admin 或 staff");
            }
            staff.setRole(role);
            staff.setDoctorId(null);
            if ("admin".equals(role)) {
                staff.setServiceStoreId(null);
            } else if ("store_admin".equals(role) && staff.getServiceStoreId() == null) {
                return Result.error("分店管理员必须绑定服务门店");
            }
            // admin和staff角色：如果传入了storeId，则设置为店铺管理员；如果为null，则为平台管理员
            // 这里允许前端传入storeId，实现多商家模式
            
            Admin created = adminService.createAdmin(staff);
            created.setPassword(null); // 不返回密码
            
            // 如果是员工角色，同步到ServiceMember
            if ("staff".equals(created.getRole())) {
                serviceMemberService.syncMemberWithAdmin(adminService.getById(created.getId()));
            }
            
            return Result.success(created);
        } catch (Exception e) {
            log.error("添加账号失败", e);
            String errorMsg = e.getMessage();
            // 提供更友好的错误提示
            if (errorMsg != null && errorMsg.contains("用户名已存在")) {
                return Result.error("用户名已存在，请使用其他用户名");
            }
            return Result.error("添加账号失败，请稍后重试");
        }
    }
    
    /**
     * 更新员工或商家信息
     */
    @PutMapping("/staff/update")
    @Operation(summary = "更新员工或商家信息")
    public Result<Boolean> updateStaff(@RequestBody Admin staff, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            String role = normalizeRole(staff.getRole());
            if (!"admin".equals(role) && !"staff".equals(role) && !"store_admin".equals(role)) {
                return Result.error("角色只能是：admin、store_admin 或 staff");
            }
            // 自我保护：不允许把自己的账号降级为非 admin（防止最后一个管理员把自己锁死）
            Long currentAdminId = (Long) request.getAttribute("adminId");
            if (currentAdminId != null && currentAdminId.equals(staff.getId()) && !"admin".equals(role)) {
                return Result.error("不能降级当前登录账号的角色");
            }
            // 唯一管理员保护：把任意 admin 降级前，确认系统中还有其他可用 admin
            Admin target = staff.getId() != null ? adminService.getById(staff.getId()) : null;
            if (target != null && "admin".equals(normalizeRole(target.getRole())) && !"admin".equals(role)) {
                long activeAdmins = adminService.listAllStaffWithDisabled().stream()
                        .filter(a -> "admin".equals(normalizeRole(a.getRole())) && !"disabled".equalsIgnoreCase(String.valueOf(a.getStatus())))
                        .count();
                if (activeAdmins <= 1) {
                    return Result.error("系统仅剩一个管理员账号，不能降级");
                }
            }
            staff.setRole(role);
            staff.setDoctorId(null);
            if ("admin".equals(role)) {
                staff.setServiceStoreId(null);
            } else if ("store_admin".equals(role) && staff.getServiceStoreId() == null) {
                return Result.error("分店管理员必须绑定服务门店");
            }
            // 统一部门名：宠物医疗 -> 宠物医院部门，与展示一致
            if ("宠物医疗".equals(staff.getDepartment())) {
                staff.setDepartment("宠物医院部门");
            }
            // admin和staff角色：如果传入了storeId，则设置为店铺管理员；如果为null，则为平台管理员
            // 这里允许前端传入storeId，实现多商家模式

            boolean success = adminService.updateAdmin(staff);
            if (success && staff.getId() != null && "staff".equals(staff.getRole())) {
                // 传入请求体 staff，以保留 maxTasksPerDay 等非持久化字段
                Admin dbAdmin = adminService.getById(staff.getId());
                if (dbAdmin != null) {
                    if (staff.getMaxTasksPerDay() != null) {
                        dbAdmin.setMaxTasksPerDay(staff.getMaxTasksPerDay());
                    }
                    serviceMemberService.syncMemberWithAdmin(dbAdmin);
                }
            }
            return success ? Result.success(true) : Result.error("更新失败");
        }, "更新账号信息失败，请稍后重试", log);
    }
    
    /**
     * 管理员创建C端用户（免短信验证码——由管理员后台发起，非匿名注册通道）
     */
    @PostMapping("/user/create")
    @Operation(summary = "管理员创建用户")
    public Result<Map<String, Object>> createUserByAdmin(@RequestBody Map<String, String> body) {
        try {
            if (userService == null) {
                return Result.error("用户服务不可用");
            }
            String phone = body == null ? null : body.get("phone");
            String password = body == null ? null : body.get("password");
            String nickname = body == null ? null : body.get("nickname");
            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            userService.register(phone.trim(), password, nickname);
            User created = userService.getUserByPhone(phone.trim());
            Map<String, Object> data = new HashMap<>();
            data.put("userId", created != null ? created.getId() : null);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            log.error("管理员创建用户失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("管理员创建用户失败", e);
            return Result.error("创建用户失败，请稍后重试");
        }
    }

    /**
     * 管理员删除用户（C端用户）
     */
    @DeleteMapping("/user/{id}")
    @Operation(summary = "管理员删除用户")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            if (userService == null) {
                return Result.error("用户服务不可用");
            }
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                userService.clearUserCache(id);
                return Result.success(true);
            }
            return Result.error("用户不存在或删除失败");
        }, "删除用户失败，请稍后重试", log);
    }
    
    /**
     * 删除员工
     */
    @DeleteMapping("/staff/{id}")
    @Operation(summary = "删除员工")
    public Result<Boolean> deleteStaff(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 自我保护：不允许删除当前登录账号
            Long currentAdminId = (Long) request.getAttribute("adminId");
            if (currentAdminId != null && currentAdminId.equals(id)) {
                return Result.error("不能删除当前登录账号");
            }
            Admin admin = adminService.getById(id);
            if (admin == null) {
                // 可能是团队管理页的 ServiceMember 回退展示（id 为 ServiceMember.id），尝试删 ServiceMember
                if (hasPendingAppointmentsByMemberId(id)) {
                    return Result.error("该成员存在待处理/已确认的预约，请先处理或转移后再删除");
                }
                serviceMemberService.deleteMember(id);
                return Result.success(true);
            }
            if ("admin".equals(normalizeRole(admin.getRole()))) {
                if ("admin".equalsIgnoreCase(admin.getUsername())) {
                    return Result.error("不能删除默认管理员账号");
                }
                // 唯一管理员保护：删除 admin 前确认系统还有其他可用 admin
                long activeAdmins = adminService.listAllStaffWithDisabled().stream()
                        .filter(a -> "admin".equals(normalizeRole(a.getRole())) && !"disabled".equalsIgnoreCase(String.valueOf(a.getStatus())))
                        .count();
                if (activeAdmins <= 1) {
                    return Result.error("系统仅剩一个管理员账号，不能删除");
                }
            } else if ("staff".equals(normalizeRole(admin.getRole()))) {
                // 删除服务人员前校验其名下是否还有未完成预约，防止悬空引用
                ServiceMember sm = serviceMemberService.getMemberByUserId(admin.getId());
                if (sm != null && hasPendingAppointmentsByMemberId(sm.getId())) {
                    return Result.error("该成员存在待处理/已确认的预约，请先处理或转移后再删除");
                }
            }
            boolean success = adminService.deleteAdmin(id);
            return success ? Result.success(true) : Result.error("删除失败");
        }, "删除员工失败，请稍后重试", log);
    }

    /**
     * 校验服务人员名下是否存在未完成（待处理/已确认）预约
     */
    private boolean hasPendingAppointmentsByMemberId(Long memberId) {
        try {
            if (doorCleaningAppointmentService != null) {
                boolean door = doorCleaningAppointmentService.getAppointmentsByMemberId(memberId).stream()
                        .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()));
                if (door) return true;
            }
            if (groomingAppointmentService != null) {
                boolean grooming = groomingAppointmentService.getAppointmentsByMemberId(memberId).stream()
                        .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()));
                if (grooming) return true;
            }
            if (hospitalAppointmentService != null) {
                boolean hospital = hospitalAppointmentService.getAppointmentsByMemberId(memberId).stream()
                        .anyMatch(a -> "pending".equals(a.getStatus()) || "confirmed".equals(a.getStatus()));
                if (hospital) return true;
            }
        } catch (Exception e) {
            log.warn("校验成员未完成预约失败: memberId={}, err={}", memberId, e.getMessage());
        }
        return false;
    }
    
    /**
     * 根据ID查询员工
     */
    @GetMapping("/staff/{id}")
    @Operation(summary = "根据ID查询员工")
    public Result<Admin> getStaffById(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return Result.error("员工不存在");
            }
            admin.setPassword(null);
            if (admin.isStaff()) {
                ServiceMember sm = serviceMemberService.getMemberByUserId(admin.getId());
                if (sm != null) {
                    if (serviceAppointmentRatingService != null) {
                        BigDecimal avgFromRatings = serviceAppointmentRatingService.avgRatingByMemberId(sm.getId());
                        admin.setRating(avgFromRatings != null ? avgFromRatings : sm.getRating());
                    } else {
                        admin.setRating(sm.getRating());
                    }
                    admin.setTotalTasks(sm.getTotalTasks());
                    admin.setMaxTasksPerDay(sm.getMaxTasksPerDay());
                    admin.setServiceMemberId(sm.getId());
                    admin.setTodayWorkload(serviceMemberService.getMemberWorkload(sm.getId(), LocalDate.now()));
                }
            }
            return Result.success(admin);
        }, "查询员工失败，请稍后重试", log);
    }
    
    /**
     * 投诉举报分页列表（超级管理员可见）
     */
    @GetMapping("/complaints")
    @Operation(summary = "投诉举报分页列表")
    public Result<IPage<Complaint>> getComplaintPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        Page<Complaint> page = new Page<>(pageNo, pageSize);
        IPage<Complaint> result = complaintService.page(page, status, type);
        return Result.success(result);
    }
    
    /**
     * 投诉举报详情
     */
    @GetMapping("/complaints/{id}")
    @Operation(summary = "投诉举报详情")
    public Result<Complaint> getComplaintById(@PathVariable Long id) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        Complaint c = complaintService.getById(id);
        if (c == null) {
            return Result.error("记录不存在");
        }
        return Result.success(c);
    }
    
    /**
     * 更新投诉举报状态：pending-待处理，processing-处理中，resolved-已解决
     */
    @PutMapping("/complaints/{id}/status")
    @Operation(summary = "更新投诉状态")
    public Result<Boolean> updateComplaintStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        String status = body != null ? body.get("status") : null;
        if (status == null || status.trim().isEmpty()) {
            return Result.error("状态不能为空");
        }
        String normalized = status.trim();
        // 白名单校验：防止任意字符串写入状态字段
        if (!"pending".equals(normalized) && !"processing".equals(normalized) && !"resolved".equals(normalized)) {
            return Result.error("非法的投诉状态: " + normalized);
        }
        boolean ok = complaintService.updateStatus(id, normalized);
        // 修复：此前 update 失败（记录不存在等）也返回 code=200，前端误报成功
        return ok ? Result.success(true) : Result.error("投诉状态更新失败，记录可能不存在");
    }
    
    /**
     * 工作人员小程序端：获取当前登录账号的工单列表（仅自己的工单）
     * 规则与管理员后台一致，只能看到分配给自己的工单
     */
    @GetMapping("/self/work-orders")
    @Operation(summary = "工作人员获取自己的工单列表（小程序端）")
    public Result<List<Map<String, Object>>> getMyWorkOrders(HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            Long adminId = (Long) request.getAttribute("adminId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }
            ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
            if (sm == null) {
                return Result.success(Collections.emptyList());
            }
            Long memberId = sm.getId();
            List<Map<String, Object>> orders = new ArrayList<>();
            
            if (doorCleaningAppointmentService != null) {
                var list = doorCleaningAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        orders.add(toWorkOrderMap("door-cleaning", "上门铲屎", apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), apt.getDate(), apt.getAppointmentDate(),
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(),
                            apt.getRemark(), apt.getLocation()));
                    }
                }
            }
            if (groomingAppointmentService != null) {
                var list = groomingAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        Object dateVal = apt.getDate() != null ? apt.getDate() : null;
                        orders.add(toWorkOrderMap("grooming", "宠物洗护", apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), dateVal, null,
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(),
                            apt.getRemark(), apt.getLocation()));
                    }
                }
            }
            if (hospitalAppointmentService != null) {
                var list = hospitalAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        orders.add(toWorkOrderMap("hospital", "宠物医院", apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), apt.getDate(), null,
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(),
                            apt.getRemark(), apt.getLocation()));
                    }
                }
            }
            
            orders.sort((a, b) -> {
                String da = String.valueOf(a.get("date"));
                String db = String.valueOf(b.get("date"));
                int cmp = db.compareTo(da);
                if (cmp != 0) return cmp;
                return String.valueOf(b.get("timeSlot")).compareTo(String.valueOf(a.get("timeSlot")));
            });
            
            return Result.success(orders);
        }, "获取工单列表失败，请稍后重试", log);
    }

    /**
     * 工作人员小程序端：获取当前登录账号的真实评价列表（来自 service_appointment_rating）
     */
    @GetMapping("/self/reviews")
    @Operation(summary = "工作人员获取自己的真实评价列表（小程序端）")
    public Result<List<Map<String, Object>>> getMyReviews(HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            Long adminId = (Long) request.getAttribute("adminId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }
            if (serviceAppointmentRatingService == null) {
                return Result.success(Collections.emptyList());
            }
            ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
            if (sm == null) {
                return Result.success(Collections.emptyList());
            }

            List<ServiceAppointmentRating> ratings = serviceAppointmentRatingService.listByMemberId(sm.getId());
            if (ratings == null || ratings.isEmpty()) {
                return Result.success(Collections.emptyList());
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (ServiceAppointmentRating rating : ratings) {
                String appointmentType = rating.getAppointmentType();
                Long userId = rating.getUserId();
                Map<String, Object> item = new HashMap<>();
                item.put("id", rating.getId());
                item.put("appointmentType", appointmentType);
                item.put("appointmentTypeName", "door-cleaning".equals(appointmentType) ? "上门铲屎"
                    : "grooming".equals(appointmentType) ? "宠物洗护"
                    : "hospital".equals(appointmentType) ? "宠物医院" : appointmentType);
                item.put("appointmentId", rating.getAppointmentId());
                item.put("rating", rating.getRating());
                item.put("comment", rating.getComment());
                item.put("createTime", rating.getCreateTime());

                if (userId != null && userService != null) {
                    try {
                        User user = userService.getUserById(userId);
                        item.put("userName", user != null
                            ? ((user.getNickname() != null && !user.getNickname().trim().isEmpty()) ? user.getNickname() : user.getUsername())
                            : "用户" + userId);
                    } catch (Exception ignored) {
                        item.put("userName", "用户" + userId);
                    }
                } else {
                    item.put("userName", "匿名用户");
                }
                result.add(item);
            }
            return Result.success(result);
        }, "获取评价列表失败，请稍后重试", log);
    }
    
    private Map<String, Object> toWorkOrderMap(String type, String typeName, String serviceProjectName, Object id,
            String contactName, String contactPhone, Object date, Object appointmentDate,
            String timeSlot, String status, Object petId, String remark, String address) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("serviceType", type);
        m.put("serviceTypeName", typeName);
        m.put("serviceProjectName", serviceProjectName != null && !serviceProjectName.trim().isEmpty() ? serviceProjectName.trim() : null);
        m.put("contactName", contactName != null ? contactName : "-");
        m.put("contactPhone", contactPhone != null ? contactPhone : "-");
        Object d = date != null ? date : (appointmentDate != null ? appointmentDate : null);
        if (d != null) {
            String s = d.toString();
            m.put("date", s.length() > 10 && s.contains("T") ? s.substring(0, 10) : s);
        } else {
            m.put("date", "");
        }
        m.put("timeSlot", timeSlot != null ? timeSlot : "-");
        m.put("status", status != null ? status : "pending");
        m.put("petId", petId);
        if (petId != null) {
            try {
                Long pid = Long.valueOf(petId.toString());
                com.pethome.entity.Pet pet = petService.getPetById(pid);
                if (pet != null) {
                    m.put("petName", pet.getName() != null ? pet.getName() : "未命名");
                    m.put("petBreed", pet.getBreed());
                    m.put("petGender", pet.getGender());
                    m.put("petAge", pet.getAge());
                    m.put("petWeight", pet.getWeight());
                    m.put("petSpecies", pet.getSpecies());
                    m.put("petAvatar", pet.getAvatar());
                } else {
                    m.put("petName", "宠物已删除");
                }
            } catch (Exception ignored) {
                m.put("petName", "未知宠物");
            }
        } else {
            m.put("petName", null);
        }
        m.put("remark", remark != null && !remark.trim().isEmpty() ? remark.trim() : null);
        m.put("address", address != null && !address.trim().isEmpty() ? address.trim() : null);
        return m;
    }
    
    /**
     * 工作人员小程序端：更新自己的工单状态（确认/完成）
     * 使用 POST 避免小程序端 PUT 兼容问题，且校验工单归属
     */
    @PostMapping("/self/work-orders/status")
    @Operation(summary = "工作人员更新工单状态（小程序端）")
    public Result<String> updateMyWorkOrderStatus(HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        return com.pethome.common.SafeCall.of(() -> {
            Long adminId = (Long) request.getAttribute("adminId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }
            ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
            if (sm == null) {
                return Result.error("您尚未分配服务部门，无法操作工单");
            }
            Long memberId = sm.getId();
            Object idObj = body.get("id");
            Object serviceTypeObj = body.get("serviceType");
            Object statusObj = body.get("status");
            if (idObj == null || serviceTypeObj == null || statusObj == null) {
                return Result.error("参数不完整");
            }
            Long orderId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
            String serviceType = serviceTypeObj.toString();
            String status = statusObj.toString();
            if (!"confirmed".equals(status) && !"completed".equals(status) && !"cancelled".equals(status)) {
                return Result.error("无效的状态值");
            }
            String rejectReason = null;
            if ("cancelled".equals(status)) {
                Object reasonObj = body.get("rejectReason");
                if (reasonObj != null && !reasonObj.toString().trim().isEmpty()) {
                    rejectReason = reasonObj.toString().trim();
                }
            }
            boolean updated = false;
            boolean forceCancel = "cancelled".equals(status);
            if ("door-cleaning".equals(serviceType) && doorCleaningAppointmentService != null) {
                var apt = doorCleaningAppointmentService.getAppointmentById(orderId);
                if (apt != null && memberId.equals(apt.getMemberId())) {
                    updated = doorCleaningAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
                }
            } else if ("grooming".equals(serviceType) && groomingAppointmentService != null) {
                var apt = groomingAppointmentService.getAppointmentById(orderId);
                if (apt != null && memberId.equals(apt.getMemberId())) {
                    updated = groomingAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
                }
            } else if ("hospital".equals(serviceType) && hospitalAppointmentService != null) {
                var apt = hospitalAppointmentService.getAppointmentById(orderId);
                if (apt != null && memberId.equals(apt.getMemberId())) {
                    updated = hospitalAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
                }
            }
            if (updated) {
                return Result.success("状态更新成功");
            }
            return Result.error("工单不存在或您无权操作");
        }, "更新失败，请稍后重试", log);
    }

    /**
     * 管理后台/工作人员统一更新工单状态：
     * - admin/store_admin：可更新任意订单
     * - staff：仅可更新分配给自己的订单
     */
    @PostMapping("/work-orders/status")
    @Operation(summary = "统一更新工单状态（支持自动识别服务类型）")
    public Result<String> updateWorkOrderStatus(HttpServletRequest request,
                                                @RequestBody Map<String, Object> body) {
        return com.pethome.common.SafeCall.of(() -> {
            Long adminId = (Long) request.getAttribute("adminId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }

            Object idObj = body.get("id");
            Object statusObj = body.get("status");
            Object serviceTypeObj = body.get("serviceType");
            if (idObj == null || statusObj == null) {
                return Result.error("参数不完整");
            }

            Long orderId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
            String status = statusObj.toString().trim();
            if (!"confirmed".equals(status) && !"completed".equals(status) && !"cancelled".equals(status)) {
                return Result.error("无效的状态值");
            }

            String rejectReason = null;
            if ("cancelled".equals(status)) {
                Object reasonObj = body.get("rejectReason");
                if (reasonObj != null && !reasonObj.toString().trim().isEmpty()) {
                    rejectReason = reasonObj.toString().trim();
                }
            }
            boolean forceCancel = "cancelled".equals(status);

            Admin operator = adminService != null ? adminService.getById(adminId) : null;
            String role = normalizeRole(operator != null ? operator.getRole() : "staff");
            boolean isStaff = "staff".equals(role);
            boolean isStoreAdmin = "store_admin".equals(role);
            Long operatorStoreId = resolveOperatorStoreId(operator);
            Long staffMemberId = null;
            if (isStaff) {
                ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                if (sm == null) {
                    return Result.error("您尚未分配服务部门，无法操作工单");
                }
                staffMemberId = sm.getId();
            }

            List<String> candidates = buildServiceTypeCandidates(serviceTypeObj);
            for (String type : candidates) {
                boolean updated = tryUpdateWorkOrderStatus(
                        type,
                        orderId,
                        status,
                        rejectReason,
                        forceCancel,
                        isStaff,
                        staffMemberId,
                        isStoreAdmin,
                        operatorStoreId
                );
                if (updated) {
                    return Result.success("状态更新成功");
                }
            }
            return Result.error("状态更新失败，记录不存在");
        }, "更新失败，请稍后重试", log);
    }

    private List<String> buildServiceTypeCandidates(Object serviceTypeObj) {
        String normalized = serviceTypeObj == null ? "" : serviceTypeObj.toString().trim().toLowerCase();
        if ("door-cleaning".equals(normalized) || "litter".equals(normalized)) {
            return Arrays.asList("door-cleaning", "grooming", "hospital");
        }
        if ("grooming".equals(normalized)) {
            return Arrays.asList("grooming", "door-cleaning", "hospital");
        }
        if ("hospital".equals(normalized)) {
            return Arrays.asList("hospital", "door-cleaning", "grooming");
        }
        return Arrays.asList("door-cleaning", "grooming", "hospital");
    }

    private boolean tryUpdateWorkOrderStatus(String serviceType,
                                             Long orderId,
                                             String status,
                                             String rejectReason,
                                             boolean forceCancel,
                                             boolean isStaff,
                                             Long staffMemberId,
                                             boolean isStoreAdmin,
                                             Long operatorStoreId) {
        if ("door-cleaning".equals(serviceType) && doorCleaningAppointmentService != null) {
            var apt = doorCleaningAppointmentService.getAppointmentById(orderId);
            if (apt == null) return false;
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) return false;
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) return false;
            return doorCleaningAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        if ("grooming".equals(serviceType) && groomingAppointmentService != null) {
            var apt = groomingAppointmentService.getAppointmentById(orderId);
            if (apt == null) return false;
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) return false;
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) return false;
            return groomingAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        if ("hospital".equals(serviceType) && hospitalAppointmentService != null) {
            var apt = hospitalAppointmentService.getAppointmentById(orderId);
            if (apt == null) return false;
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) return false;
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) return false;
            return hospitalAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        return false;
    }

    private Long resolveOperatorStoreId(Admin operator) {
        if (operator == null) return null;
        if (operator.getServiceStoreId() != null) return operator.getServiceStoreId();
        return operator.getStoreId();
    }

    private boolean canStoreAdminOperate(Long appointmentStoreId, Long operatorStoreId) {
        if (operatorStoreId == null) return false;
        return appointmentStoreId != null && appointmentStoreId.equals(operatorStoreId);
    }
    
    /**
     * 获取员工的预约/任务列表（用于团队管理-成员详情的项目参与、任务分配）
     */
    @GetMapping("/staff/{adminId}/appointments")
    @Operation(summary = "获取员工的预约任务列表")
    public Result<Map<String, Object>> getStaffAppointments(@PathVariable Long adminId) {
        return com.pethome.common.SafeCall.of(() -> {
            ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
            if (sm == null) {
                return Result.success(Map.of(
                    "projects", List.of(),
                    "tasks", List.of()
                ));
            }
            Long memberId = sm.getId();
            List<Map<String, Object>> projects = new java.util.ArrayList<>();
            List<Map<String, Object>> tasks = new java.util.ArrayList<>();
            String serviceType = sm.getServiceType();
            String serviceTypeName = resolveDepartmentName(serviceType);
            int completedCount = 0;
            int totalCount = 0;
            
            if (doorCleaningAppointmentService != null) {
                var list = doorCleaningAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        totalCount++;
                        if ("completed".equals(apt.getStatus())) completedCount++;
                        tasks.add(toTaskMap("door-cleaning", "上门铲屎", apt.getId(), apt.getDate(), apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                    }
                }
            }
            if (groomingAppointmentService != null) {
                var list = groomingAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        totalCount++;
                        if ("completed".equals(apt.getStatus())) completedCount++;
                        tasks.add(toTaskMap("grooming", "宠物洗护", apt.getId(), apt.getDate(), apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                    }
                }
            }
            if (hospitalAppointmentService != null) {
                var list = hospitalAppointmentService.getAppointmentsByMemberId(memberId);
                if (list != null) {
                    for (var apt : list) {
                        totalCount++;
                        if ("completed".equals(apt.getStatus())) completedCount++;
                        tasks.add(toTaskMap("hospital", "宠物医院", apt.getId(), apt.getDate(), apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                    }
                }
            }
            
            projects.add(Map.<String, Object>of(
                "name", serviceTypeName,
                "role", "服务人员",
                "progress", totalCount > 0 ? (int)(100.0 * completedCount / totalCount) : 0,
                "totalCount", totalCount,
                "completedCount", completedCount
            ));
            
            tasks.sort((a, b) -> {
                String da = String.valueOf(a.get("date"));
                String db = String.valueOf(b.get("date"));
                int cmp = db.compareTo(da);
                return cmp != 0 ? cmp : String.valueOf(b.get("timeSlot")).compareTo(String.valueOf(a.get("timeSlot")));
            });
            
            return Result.success(Map.of("projects", projects, "tasks", tasks));
        }, "获取预约列表失败，请稍后重试", log);
    }
    
    private Map<String, Object> toTaskMap(String type, String typeName, Object id, Object date, Object timeSlot, Object status, Object contact) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("project", typeName);
        m.put("title", (contact != null ? contact : "") + " - " + typeName);
        m.put("date", date != null ? date.toString() : "");
        m.put("timeSlot", timeSlot != null ? timeSlot : "");
        m.put("dueDate", date != null ? date.toString() : "");
        m.put("status", status != null ? status : "");
        return m;
    }
    
    /**
     * 密码加密测试接口（用于迁移时加密密码）
     */
    @GetMapping("/test/encrypt")
    @Operation(summary = "密码加密测试接口")
    public Result<String> encryptPassword(@RequestParam String password) {
        return com.pethome.common.SafeCall.of(() -> {
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            String encrypted = adminService.encodePassword(password);
            return Result.success(encrypted);
        }, "加密失败，请稍后重试", log);
    }

    private String resolveDepartmentName(String serviceType) {
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return "未分配部门";
        }
        switch (serviceType) {
            case "door-cleaning":
                return "上门铲屎部门";
            case "grooming":
                return "宠物洗护部门";
            case "hospital":
                return "宠物医院部门";
            default:
                return serviceType;
        }
    }
}


