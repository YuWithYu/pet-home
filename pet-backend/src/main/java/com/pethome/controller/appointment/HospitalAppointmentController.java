package com.pethome.controller.appointment;

import com.pethome.common.Result;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/hospital-appointments")
@Tag(name = "宠物医院预约管理")
public class HospitalAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(HospitalAppointmentController.class);

    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;

    @Autowired
    private com.pethome.service.admin.AdminService adminService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;

    @Autowired
    private com.pethome.service.appointment.AppointmentDelayService appointmentDelayService;

    @GetMapping("/page")
    @Operation(summary = "分页查询宠物医院预约")
    public Result<IPage<HospitalAppointment>> getHospitalAppointmentPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "服务类型") @RequestParam(required = false) String serviceType,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：分页结果含客户姓名、手机号与地址，属门店运营数据；
        // 此前任意登录用户（含 C 端 user）都可调用并按 storeId 读取全平台预约
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查看医院预约列表");
        }

        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null && !"admin".equalsIgnoreCase((String) request.getAttribute("role"))) {
            com.pethome.entity.Admin admin = adminService.getById(adminId);
            if (admin != null) {
                Long effectiveStoreId = admin.getServiceStoreId();
                if (effectiveStoreId == null && serviceMemberService != null) {
                    com.pethome.entity.ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                    if (sm != null && sm.getStoreId() != null) {
                        effectiveStoreId = sm.getStoreId();
                    }
                }
                if (effectiveStoreId != null) {
                    storeId = effectiveStoreId;
                } else {
                    storeId = -1L;
                }
            }
        }

        Page<HospitalAppointment> page = new Page<>(current, size);
        IPage<HospitalAppointment> result = hospitalAppointmentService.getAppointmentPage(page, status, keyword, serviceType, storeId);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @Operation(summary = "获取用户宠物医院预约列表")
    public Result<List<HospitalAppointment>> getUserHospitalAppointments(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：归属校验——userId 必须以登录态为准（拦截器写入的 userId attribute），
        // 路径参数仅兼容旧前端；未登录或与登录身份不一致时视为越权，返回空列表。
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null || !currentUserId.equals(userId)) {
            return Result.success(java.util.Collections.emptyList());
        }
        List<HospitalAppointment> appointments = hospitalAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @GetMapping("/member/list")
    @Operation(summary = "获取服务人员的宠物医院预约列表")
    public Result<List<HospitalAppointment>> getMemberHospitalAppointments(@RequestParam Long memberId,
                                                                           HttpServletRequest request) {
        // 安全修复：服务人员维度查询会返回客户手机号/地址等敏感信息，仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查询服务人员预约列表");
        }
        // 安全修复：分店账号不得查看其他门店服务人员的预约（含客户手机号/地址）
        Long scope = scopedStoreId(request);
        if (scope != null) {
            com.pethome.entity.ServiceMember member = serviceMemberService.getMemberById(memberId);
            if (member == null) {
                return Result.error(404, "服务人员不存在");
            }
            if (!scope.equals(member.getStoreId())) {
                log.warn("[HospitalAppointment] member/list 跨门店拒绝: scope={}, memberStore={}", scope, member.getStoreId());
                return Result.error(403, "无权查询其他门店的服务人员预约");
            }
        }
        List<HospitalAppointment> appointments = hospitalAppointmentService.getAppointmentsByMemberId(memberId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @Operation(summary = "创建宠物医院预约")
    public Result<HospitalAppointment> createHospitalAppointment(@RequestBody HospitalAppointment appointment,
                                                                 javax.servlet.http.HttpServletRequest request) {
        try {
            // 安全修复：归属强制以登录态为准（拦截器解析 token 后写入的 userId attribute），
            // body 中的 userId 一律覆盖，杜绝伪造他人归属创建预约
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            appointment.setUserId(currentUserId);
            if (appointment.getDate() == null) {
                return Result.error("预约日期不能为空");
            }
            if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
                return Result.error("预约时间段不能为空");
            }

            if (appointment.getServiceType() == null || appointment.getServiceType().isEmpty()) {
                appointment.setServiceType("hospital");
            }
            boolean success = hospitalAppointmentService.createHospitalAppointment(appointment);
            if (success) {
                return Result.success(appointment);
            } else {
                return Result.error("预约创建失败");
            }
        } catch (Exception e) {
            log.error("预约创建失败", e);
            return Result.error("预约创建失败，请稍后重试");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物医院预约详情")
    public Result<HospitalAppointment> getHospitalAppointmentDetail(@PathVariable Long id,
                                                                     HttpServletRequest request) {
        HospitalAppointment appointment = hospitalAppointmentService.getAppointmentById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        // 安全修复：详情含手机号/地址等个人信息——staff/admin 仅限本人门店，普通用户仅能查看本人预约
        if (isStaffOrAdmin(request)) {
            if (!canAccessStore(request, appointment.getStoreId())) {
                return Result.error(403, "无权查看此预约");
            }
        } else {
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null || !currentUserId.equals(appointment.getUserId())) {
                return Result.error(403, "无权查看此预约");
            }
        }
        return Result.success(appointment);
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /**
     * 当前管理端账号的可见门店：平台管理员返回 null（不限门店），
     * 店长/店员返回其服务门店（无门店归属返回 -1，保证查不到任何数据）。
     * 用于防止分店账号跨门店查看/操作其它门店的预约。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (!isStaffOrAdmin(request)) {
            return null;
        }
        Object roleAttr = request.getAttribute("role");
        if (roleAttr != null && "admin".equalsIgnoreCase(String.valueOf(roleAttr))) {
            return null;
        }
        Object storeAttr = request.getAttribute("serviceStoreId");
        if (storeAttr instanceof Number) {
            return ((Number) storeAttr).longValue();
        }
        Object adminIdAttr = request.getAttribute("adminId");
        if (adminIdAttr instanceof Number) {
            com.pethome.entity.ServiceMember sm =
                    serviceMemberService.getMemberByUserId(((Number) adminIdAttr).longValue());
            if (sm != null && sm.getStoreId() != null) {
                return sm.getStoreId();
            }
        }
        return -1L;
    }

    /** 门店归属校验：返回 true 表示可访问 */
    private boolean canAccessStore(HttpServletRequest request, Long storeId) {
        Long scope = scopedStoreId(request);
        return scope == null || (storeId != null && scope.equals(storeId));
    }

    /**
     * 预约状态变更权限校验。
     * @return null 表示放行；非 null 为拒绝原因
     */
    private String checkStatusPermission(Long id, HttpServletRequest request) {
        if (isStaffOrAdmin(request)) {
            // 安全修复：分店账号只能操作本门店的预约
            HospitalAppointment appointment = hospitalAppointmentService.getAppointmentById(id);
            if (appointment == null) {
                return "预约不存在";
            }
            if (!canAccessStore(request, appointment.getStoreId())) {
                return "无权操作此预约";
            }
            return null;
        }
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null) {
            return "未登录或登录已过期";
        }
        HospitalAppointment appointment = hospitalAppointmentService.getAppointmentById(id);
        if (appointment == null || !currentUserId.equals(appointment.getUserId())) {
            return "无权操作此预约";
        }
        return null;
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "分配服务人员到预约")
    public Result<String> assignMember(
            @PathVariable Long id,
            @RequestParam Long memberId,
            HttpServletRequest request) {
        try {
            // 安全修复：分配服务人员属运营操作，仅限 staff/admin，防止用户端擅自改派
            if (!isStaffOrAdmin(request)) {
                return Result.error(403, "无权分配服务人员");
            }
            // 安全修复：分店账号不得跨门店分配（预约与本门店、服务人员门店双重校验）
            HospitalAppointment target = hospitalAppointmentService.getAppointmentById(id);
            if (target == null) {
                return Result.error(404, "预约不存在");
            }
            if (!canAccessStore(request, target.getStoreId())) {
                return Result.error(403, "无权操作此预约");
            }
            Long scope = scopedStoreId(request);
            if (scope != null) {
                com.pethome.entity.ServiceMember member = serviceMemberService.getMemberById(memberId);
                if (member == null) {
                    return Result.error(404, "服务人员不存在");
                }
                if (!scope.equals(member.getStoreId())) {
                    log.warn("[HospitalAppointment] assign 跨门店拒绝: scope={}, memberStore={}", scope, member.getStoreId());
                    return Result.error(403, "无权分配其他门店的服务人员");
                }
            }
            boolean success = hospitalAppointmentService.assignMember(id, memberId);
            if (success) {
                HospitalAppointment apt = hospitalAppointmentService.getById(id);
                if (apt != null) {
                    java.time.LocalDateTime aptTime = apt.getDate() != null ? apt.getDate().atStartOfDay() : null;
                    appointmentDelayService.registerReminderKeys("hospital", id, apt.getUserId(), aptTime, "医院预约");
                }
                return Result.success("分配成功");
            } else {
                return Result.error("分配失败，请确认服务人员存在且类型为医院");
            }
        } catch (Exception e) {
            log.error("分配失败", e);
            return Result.error("分配失败，请稍后重试");
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新宠物医院预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateHospitalAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart,
            HttpServletRequest request) {
        try {
            // 安全修复：状态接口越权防护——
            // 普通用户仅允许取消自己的预约（status=cancelled 且归属本人）；
            // confirmed/rejected/completed 等运营态变更仅限 staff/admin，防止用户绕过审核自审自发
            String denial = checkStatusPermission(id, request);
            if (denial != null) {
                return Result.error(403, denial);
            }
            if (!isStaffOrAdmin(request) && !"cancelled".equals(status)) {
                return Result.error(403, "无权变更预约状态");
            }
            String reason = (rejectReason != null && !rejectReason.trim().isEmpty()) ? rejectReason.trim() : null;
            boolean success = hospitalAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("状态更新失败", e);
            return Result.error("状态更新失败，请稍后重试");
        }
    }
}
