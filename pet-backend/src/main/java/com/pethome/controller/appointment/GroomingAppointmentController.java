package com.pethome.controller.appointment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingAppointment;
import com.pethome.service.appointment.GroomingAppointmentService;
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
@RequestMapping("/api/grooming-appointments")
@Tag(name = "宠物洗护预约管理")
public class GroomingAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(GroomingAppointmentController.class);

    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired
    private com.pethome.service.admin.AdminService adminService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;

    @GetMapping("/page")
    @Operation(summary = "分页查询宠物洗护预约")
    public Result<IPage<GroomingAppointment>> getGroomingAppointmentPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "服务类型") @RequestParam(required = false) String serviceType,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {

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

        Page<GroomingAppointment> page = new Page<>(current, size);
        IPage<GroomingAppointment> result = groomingAppointmentService.getAppointmentPage(page, status, keyword, serviceType, storeId);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @Operation(summary = "获取用户宠物洗护预约列表")
    public Result<List<GroomingAppointment>> getUserGroomingAppointments(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：归属校验——userId 必须以登录态为准（拦截器写入的 userId attribute），
        // 路径参数仅兼容旧前端；未登录或与登录身份不一致时视为越权，返回空列表。
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null || !currentUserId.equals(userId)) {
            return Result.success(java.util.Collections.emptyList());
        }
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @GetMapping("/member/list")
    @Operation(summary = "获取服务人员的洗护预约列表")
    public Result<List<GroomingAppointment>> getMemberGroomingAppointments(@RequestParam Long memberId,
                                                                         HttpServletRequest request) {
        // 安全修复：服务人员维度查询会返回客户手机号/地址等敏感信息，仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查询服务人员预约列表");
        }
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByMemberId(memberId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @Operation(summary = "创建宠物洗护预约")
    public Result<GroomingAppointment> createGroomingAppointment(@RequestBody GroomingAppointment appointment,
                                                                 HttpServletRequest request) {
        try {
            // 安全修复：归属强制以登录态为准（拦截器解析 token 后写入的 userId attribute），
            // body 中的 userId 一律覆盖，杜绝伪造他人归属创建预约
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            appointment.setUserId(currentUserId);
            boolean success = groomingAppointmentService.createGroomingAppointment(appointment);
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
    @Operation(summary = "获取宠物洗护预约详情")
    public Result<GroomingAppointment> getGroomingAppointmentDetail(@PathVariable Long id,
                                                                    HttpServletRequest request) {
        GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        // 安全修复：详情含手机号/地址等个人信息——staff/admin 可查看全部，普通用户仅能查看本人预约
        if (!isStaffOrAdmin(request)) {
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null || !currentUserId.equals(appointment.getUserId())) {
                return Result.error(403, "无权查看此预约");
            }
        }
        return Result.success(appointment);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新宠物洗护预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateGroomingAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart,
            HttpServletRequest request) {
        try {
            // 安全修复：状态接口越权防护——
            // 普通用户仅允许取消自己的预约（status=cancelled 且归属本人）；
            // confirmed/rejected/completed 等运营态变更仅限 staff/admin，防止用户绕过审核自审自发
            String denial = checkStatusPermission(id, status, request);
            if (denial != null) {
                return Result.error(403, denial);
            }
            String reason = (rejectReason != null && !rejectReason.trim().isEmpty()) ? rejectReason.trim() : null;
            boolean success = groomingAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart);
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

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /**
     * 预约状态变更权限校验。
     * @return null 表示放行；非 null 为拒绝原因
     */
    private String checkStatusPermission(Long id, String status, HttpServletRequest request) {
        if (isStaffOrAdmin(request)) {
            return null;
        }
        // 普通用户：仅允许取消本人预约
        if (!"cancelled".equals(status)) {
            return "无权变更预约状态";
        }
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null) {
            return "未登录或登录已过期";
        }
        GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
        if (appointment == null || !currentUserId.equals(appointment.getUserId())) {
            return "无权操作此预约";
        }
        return null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物洗护预约")
    public Result<GroomingAppointment> updateGroomingAppointment(@PathVariable Long id, @RequestBody GroomingAppointment appointment,
                                                                   HttpServletRequest request) {
        try {
            // 安全修复：全字段更新仅限 staff/admin（普通用户可借此改价格/归属等敏感字段）
            if (!isStaffOrAdmin(request)) {
                return Result.error(403, "无权修改此预约");
            }
            appointment.setId(id);
            boolean success = groomingAppointmentService.updateById(appointment);
            if (success) {
                return Result.success("预约更新成功", appointment);
            } else {
                return Result.error("预约更新失败");
            }
        } catch (Exception e) {
            log.error("预约更新失败", e);
            return Result.error("预约更新失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物洗护预约")
    public Result<Boolean> deleteGroomingAppointment(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 安全修复：物理删除仅限 staff/admin
            if (!isStaffOrAdmin(request)) {
                return Result.error(403, "无权删除此预约");
            }
            boolean success = groomingAppointmentService.removeById(id);
            if (success) {
                return Result.success("预约删除成功", true);
            } else {
                return Result.error("预约删除失败");
            }
        } catch (Exception e) {
            log.error("预约删除失败", e);
            return Result.error("预约删除失败，请稍后重试");
        }
    }
}

