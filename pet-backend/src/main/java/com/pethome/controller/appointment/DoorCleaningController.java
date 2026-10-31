package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 上门铲屎服务兼容接口控制器
 * 提供与前端小程序兼容的接口路径
 */
@RestController
@RequestMapping("/api/door-cleaning")
@Tag(name = "上门铲屎服务兼容接口")
public class DoorCleaningController {

    private static final Logger logger = LoggerFactory.getLogger(DoorCleaningController.class);

    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    @Autowired
    private com.pethome.service.admin.AdminService adminService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;

    @Autowired
    private com.pethome.service.appointment.AppointmentDelayService appointmentDelayService;

    @PostMapping("/create")
    @Operation(summary = "创建上门铲屎预约")
    public Result<DoorCleaningAppointment> createAppointment(@RequestBody DoorCleaningAppointment appointment,
                                                              HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：归属强制以登录态为准（拦截器解析 token 后写入的 userId attribute），
            // body 中的 userId 一律覆盖，杜绝伪造他人归属创建预约
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            appointment.setUserId(currentUserId);
            if (appointment.getDate() == null && appointment.getAppointmentDate() == null) {
                return Result.error("预约日期不能为空");
            }
            if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
                return Result.error("预约时间段不能为空");
            }

            boolean saved = doorCleaningAppointmentService.createDoorCleaningAppointment(appointment);
            if (saved) {
                LocalDateTime aptTime = appointment.getAppointmentDate() != null ? appointment.getAppointmentDate() :
                        (appointment.getDate() != null ? appointment.getDate().atStartOfDay() : null);
                appointmentDelayService.registerDelayKeys("door-cleaning", appointment.getId(), aptTime, appointment.getTimeSlot());
                appointmentDelayService.registerReminderKeys("door-cleaning", appointment.getId(), appointment.getUserId(), aptTime, "上门铲屎预约");
                return Result.success(appointment);
            }
            return Result.error("创建预约失败");
        }, "创建预约失败，请稍后重试", logger);
    }

    @GetMapping("/user/list/{userId}")
    @Operation(summary = "获取用户上门铲屎预约列表")
    public Result<List<DoorCleaningAppointment>> getUserAppointments(@PathVariable Long userId, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：归属校验——userId 必须以登录态为准（拦截器写入的 userId attribute），
            // 路径参数仅兼容旧前端；未登录或与登录身份不一致时视为越权，返回空列表。
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null || !currentUserId.equals(userId)) {
                logger.warn("上门铲屎预约列表归属校验未通过: pathUserId={}, currentUserId={}", userId, currentUserId);
                return Result.success(java.util.Collections.emptyList());
            }
            logger.info("查询用户预约列表，userId: {}", userId);
            List<DoorCleaningAppointment> appointments = doorCleaningAppointmentService.getAppointmentsByUserId(userId);
            logger.info("查询到预约数量: {}", appointments != null ? appointments.size() : 0);
            return Result.success(appointments);
        }, "获取预约列表失败，请稍后重试", logger);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取预约详情")
    public Result<DoorCleaningAppointment> getAppointmentDetail(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            DoorCleaningAppointment appointment = doorCleaningAppointmentService.getAppointmentById(id);
            if (appointment == null) {
                return Result.error("预约不存在");
            }
            // 安全修复：详情含手机号/地址等个人信息——staff/admin 仅限本门店，普通用户仅能查看本人预约
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
            // 兼容前端：如果未设置预约日期，用date填充
            if (appointment.getAppointmentDate() == null && appointment.getDate() != null) {
                appointment.setAppointmentDate(appointment.getDate().atStartOfDay());
            }
            return Result.success(appointment);
        }, "获取预约详情失败，请稍后重试", logger);
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /**
     * 当前管理端账号的可见门店：平台管理员返回 null（不限门店），
     * 店长/店员返回其服务门店（无门店归属返回 -1，保证查不到任何数据）。
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
            DoorCleaningAppointment appointment = doorCleaningAppointmentService.getAppointmentById(id);
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
        DoorCleaningAppointment appointment = doorCleaningAppointmentService.getAppointmentById(id);
        if (appointment == null || !currentUserId.equals(appointment.getUserId())) {
            return "无权操作此预约";
        }
        return null;
    }

    @GetMapping("/member/list")
    @Operation(summary = "获取服务人员分配的上门铲屎预约列表")
    public Result<List<DoorCleaningAppointment>> getMemberAppointments(@RequestParam Long memberId,
                                                                      HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：服务人员维度查询会返回客户手机号/地址等敏感信息，仅限 staff/admin
            if (!isStaffOrAdmin(request)) {
                return Result.error(403, "无权查询服务人员预约列表");
            }
            // 安全修复：分店账号不得查看其他门店服务人员的预约
            Long scope = scopedStoreId(request);
            if (scope != null) {
                com.pethome.entity.ServiceMember member = serviceMemberService.getMemberById(memberId);
                if (member == null) {
                    return Result.error(404, "服务人员不存在");
                }
                if (!scope.equals(member.getStoreId())) {
                    logger.warn("door-cleaning member/list 跨门店拒绝: scope={}, memberStore={}", scope, member.getStoreId());
                    return Result.error(403, "无权查询其他门店的服务人员预约");
                }
            }
            List<DoorCleaningAppointment> appointments = doorCleaningAppointmentService.getAppointmentsByMemberId(memberId);
            return Result.success(appointments);
        }, "获取预约列表失败，请稍后重试", logger);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询上门铲屎预约列表（管理后台）")
    public Result<IPage<DoorCleaningAppointment>> getAppointmentPage(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request
    ) {
        // 安全修复：分页结果含客户姓名、手机号与地址，属门店运营数据；
        // 此前任意登录用户（含 C 端 user）都可调用并按 storeId 读取全平台预约
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查看上门铲屎预约列表");
        }
        return com.pethome.common.SafeCall.of(() -> {
            Long queryStoreId = storeId;
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
                        queryStoreId = effectiveStoreId;
                    } else {
                        queryStoreId = -1L;
                    }
                }
            }
            Page<DoorCleaningAppointment> page = new Page<>(pageNo, pageSize);
            IPage<DoorCleaningAppointment> result = doorCleaningAppointmentService.getAppointmentPage(page, status, keyword, serviceType, queryStoreId);
            return Result.success(result);
        }, "获取预约列表失败，请稍后重试", logger);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新上门铲屎预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status,
                                       @RequestParam(required = false) String rejectReason,
                                       @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart,
                                       HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
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
            boolean updated = doorCleaningAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart);
            if (updated) {
                return Result.success("状态更新成功");
            }
            return Result.error("状态更新失败，记录不存在");
        }, "更新预约状态失败，请稍后重试", logger);
    }
}

