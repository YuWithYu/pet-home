package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.Appointment;
import com.pethome.service.appointment.AppointmentService;
import com.pethome.security.AdminContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一预约管理控制器
 * 支持管理员和服务人员操作
 */
@RestController
@RequestMapping("/api/appointment")
@Tag(name = "预约管理")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired(required = false)
    private AdminContext adminContext;

    @Autowired
    private com.pethome.service.appointment.AppointmentDelayService appointmentDelayService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;

    /** 管理端账号关联的服务人员 ID（staff 的工单归属判定用） */
    private Long resolveMemberId(Admin admin) {
        if (admin == null) {
            return null;
        }
        com.pethome.entity.ServiceMember member = serviceMemberService.getMemberByUserId(admin.getId());
        return member != null ? member.getId() : null;
    }

    /** 受限门店：平台管理员返回 null（不限门店），其它账号返回其服务门店（无归属返回 -1） */
    private Long resolveScopedStoreId(Admin admin) {
        if (admin == null || admin.isSuperAdmin()) {
            return null;
        }
        Long storeId = admin.getServiceStoreId();
        if (storeId == null) {
            com.pethome.entity.ServiceMember member = serviceMemberService.getMemberByUserId(admin.getId());
            if (member != null) {
                storeId = member.getStoreId();
            }
        }
        return storeId != null ? storeId : -1L;
    }

    /** 本门店服务人员 ID 集合；平台管理员返回 null 表示不限制 */
    private java.util.Set<Long> allowedMemberIds(Admin admin) {
        Long scope = resolveScopedStoreId(admin);
        if (scope == null) {
            return null;
        }
        java.util.Set<Long> ids = new java.util.HashSet<>();
        List<com.pethome.entity.ServiceMember> members = serviceMemberService.getAllMembers();
        if (members != null) {
            for (com.pethome.entity.ServiceMember member : members) {
                if (member.getId() != null && scope.equals(member.getStoreId())) {
                    ids.add(member.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 预约归属校验：平台管理员不限；staff 仅限本人工单；店长仅限本门店服务人员的工单。
     * 修复原实现用 adminId 直接与 memberId 比较（id 语义不一致，可能命中他人工单）。
     */
    private boolean canAccessAppointment(Admin admin, Appointment appointment) {
        if (admin == null || appointment == null) {
            return false;
        }
        if (admin.isSuperAdmin()) {
            return true;
        }
        Long memberId = appointment.getMemberId();
        if (memberId == null) {
            return false;
        }
        if ("staff".equals(admin.getRole())) {
            Long ownMemberId = resolveMemberId(admin);
            return ownMemberId != null && ownMemberId.equals(memberId);
        }
        java.util.Set<Long> allowed = allowedMemberIds(admin);
        return allowed != null && allowed.contains(memberId);
    }
    
    /**
     * 添加预约（小程序端用户）
     */
    @PostMapping("/add")
    @Operation(summary = "添加预约")
    public Result<Appointment> addAppointment(@RequestBody Appointment appointment,
                                              javax.servlet.http.HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：归属强制以登录态为准（拦截器解析 token 后写入的 userId attribute），
            // body 中的 userId 一律覆盖，杜绝伪造他人归属创建预约
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            appointment.setUserId(currentUserId);
            // 验证必填字段
            if (appointment.getServiceType() == null || appointment.getServiceType().trim().isEmpty()) {
                return Result.error("服务类型不能为空");
            }
            if (appointment.getDate() == null) {
                return Result.error("预约日期不能为空");
            }
            if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
                return Result.error("时间段不能为空");
            }
            
            // 检查时间段是否可用
            boolean available = appointmentService.checkTimeSlotAvailable(
                appointment.getServiceType(),
                appointment.getDate(),
                appointment.getTimeSlot(),
                appointment.getMemberId()
            );
            
            if (!available) {
                return Result.error("该时间段已被占用，请选择其他时间");
            }
            
            // 创建预约
            Appointment created = appointmentService.createAppointment(appointment);
            if (created != null && created.getId() != null) {
                String st = appointment.getServiceType();
                java.time.LocalDateTime aptTime = created.getDate() != null ? created.getDate().atStartOfDay() : null;
                appointmentDelayService.registerDelayKeys(st, created.getId(), aptTime, created.getTimeSlot());
                String svcName = "grooming".equals(st) ? "洗护预约" : ("hospital".equals(st) ? "医院预约" : "服务预约");
                appointmentDelayService.registerReminderKeys(st, created.getId(), created.getUserId(), aptTime, svcName);
            }
            return Result.success(created);
        }, "添加预约失败，请稍后重试", log);
    }
    
    /**
     * 检查时间段是否可用
     */
    @GetMapping("/checkAvailable")
    @Operation(summary = "检查时间段是否可用")
    public Result<Map<String, Object>> checkAvailable(@RequestParam String serviceType,
                                                       @RequestParam String date,
                                                       @RequestParam String timeSlot,
                                                       @RequestParam(required = false) Long memberId) {
        return com.pethome.common.SafeCall.of(() -> {
            LocalDate localDate = LocalDate.parse(date);
            boolean available = appointmentService.checkTimeSlotAvailable(serviceType, localDate, timeSlot, memberId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("available", available);
            result.put("serviceType", serviceType);
            result.put("date", date);
            result.put("timeSlot", timeSlot);
            result.put("message", available ? "时间段可用" : "时间段已被占用");
            
            return Result.success(result);
        }, "检查失败，请稍后重试", log);
    }
    
    /**
     * 获取预约列表（管理员可查看全部，服务人员只能查看自己的）
     */
    @GetMapping("/list")
    @Operation(summary = "获取预约列表")
    public Result<List<Appointment>> listAppointments(@RequestParam(required = false) String serviceType,
                                                         @RequestParam(required = false) String date,
                                                         @RequestParam(required = false) String status,
                                                         @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Appointment> appointments;
            
            // 从 token 获取当前登录用户信息
            Admin currentAdmin = getCurrentAdminFromToken(token);
            
            if (currentAdmin != null) {
                if ("staff".equals(currentAdmin.getRole())) {
                    // 服务人员：只能查看自己的预约（adminId 需先映射到服务人员 ID）
                    Long ownMemberId = resolveMemberId(currentAdmin);
                    appointments = ownMemberId != null
                            ? appointmentService.getAppointmentsByMemberId(ownMemberId)
                            : java.util.Collections.emptyList();
                } else {
                    // 平台管理员：可以查看所有预约；店长/门店账号：仅本门店服务人员的预约
                    if (serviceType != null && date != null) {
                        LocalDate localDate = LocalDate.parse(date);
                        appointments = appointmentService.getAppointmentsByServiceTypeAndDate(serviceType, localDate);
                    } else {
                        // 查询所有预约（需要分页，这里简化处理）
                        appointments = appointmentService.getAppointmentsByDateRange(
                            LocalDate.now().minusDays(30),
                            LocalDate.now().plusDays(30)
                        );
                    }
                    java.util.Set<Long> allowedMemberIds = allowedMemberIds(currentAdmin);
                    if (allowedMemberIds != null) {
                        appointments = appointments.stream()
                                .filter(a -> a.getMemberId() != null && allowedMemberIds.contains(a.getMemberId()))
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
            } else {
                // 未登录或 token 无效，返回空列表
                return Result.error("未登录或登录已过期");
            }
            
            // 根据状态过滤
            if (status != null && !status.trim().isEmpty()) {
                final String statusFilter = status.trim();
                appointments = appointments.stream()
                    .filter(a -> statusFilter.equals(a.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success(appointments);
        }, "获取预约列表失败，请稍后重试", log);
    }

    /**
     * 服务人员查看自己的预约列表
     */
    @GetMapping("/listByStaff")
    @Operation(summary = "服务人员查看自己的预约列表")
    public Result<List<Appointment>> listByStaff(@RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            if (!"staff".equals(currentAdmin.getRole())) {
                return Result.error("该接口仅限服务人员访问");
            }
            
            // 获取服务人员的预约列表（adminId 需映射到服务人员 ID）
            Long ownMemberId = resolveMemberId(currentAdmin);
            List<Appointment> appointments = ownMemberId != null
                    ? appointmentService.getAppointmentsByMemberId(ownMemberId)
                    : java.util.Collections.emptyList();
            
            return Result.success(appointments);
        }, "获取预约列表失败，请稍后重试", log);
    }
    
    /**
     * 分配工单给服务人员（管理员操作）
     */
    @PostMapping("/assign")
    @Operation(summary = "分配工单")
    public Result<Appointment> assignAppointment(@RequestParam Long appointmentId,
                                                   @RequestParam Long memberId,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            if (!"admin".equalsIgnoreCase(currentAdmin.getRole())) {
                return Result.error("只有管理员可以分配工单");
            }
            
            Appointment appointment = appointmentService.assignAppointment(appointmentId, memberId);
            if (appointment != null && appointment.getId() != null) {
                String st = appointment.getServiceType();
                java.time.LocalDateTime aptTime = appointment.getDate() != null ? appointment.getDate().atStartOfDay() : null;
                String svcName = "grooming".equals(st) ? "洗护预约" : ("hospital".equals(st) ? "医院预约" : "服务预约");
                appointmentDelayService.registerReminderKeys(st, appointment.getId(), appointment.getUserId(), aptTime, svcName);
            }
            return Result.success(appointment);
        }, "分配工单失败，请稍后重试", log);
    }
    
    /**
     * 更新预约信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新预约信息")
    public Result<Appointment> updateAppointment(@PathVariable Long id,
                                                   @RequestBody Appointment appointment,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment existing = appointmentService.getAppointmentById(id);
            if (existing == null) {
                return Result.error("预约不存在");
            }
            
            // 安全修复：staff 仅限本人工单，店长仅限本门店工单（原实现用 adminId 与 memberId 直接比较）
            if (!canAccessAppointment(currentAdmin, existing)) {
                return Result.error(403, "无权操作此预约");
            }
            
            // 设置ID并更新
            appointment.setId(id);
            Appointment updated = appointmentService.updateAppointment(appointment);
            return Result.success(updated);
        }, "更新预约失败，请稍后重试", log);
    }
    
    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新预约状态")
    public Result<Appointment> updateStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return Result.error("预约不存在");
            }
            
            // 安全修复：staff 仅限本人工单，店长仅限本门店工单
            if (!canAccessAppointment(currentAdmin, appointment)) {
                return Result.error(403, "无权操作此预约");
            }
            
            Appointment updated = appointmentService.updateAppointmentStatus(id, status);
            return Result.success(updated);
        }, "更新状态失败，请稍后重试", log);
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预约详情")
    public Result<Appointment> getAppointmentDetail(@PathVariable Long id,
                                                      @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return Result.error("预约不存在");
            }
            
            // 安全修复：staff 仅限本人工单，店长仅限本门店工单
            if (!canAccessAppointment(currentAdmin, appointment)) {
                return Result.error(403, "无权查看此预约");
            }
            
            return Result.success(appointment);
        }, "获取预约详情失败，请稍后重试", log);
    }
    
    /**
     * 从 token 获取当前管理员信息
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
}


