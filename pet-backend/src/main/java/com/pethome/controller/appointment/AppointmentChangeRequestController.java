package com.pethome.controller.appointment;

import com.pethome.common.Result;
import com.pethome.entity.AppointmentChangeRequest;
import com.pethome.security.AppointmentStoreGuard;
import com.pethome.service.appointment.AppointmentChangeRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预约变更申请：用户提交变更；工作人员查询待处理、同意/拒绝
 */
@RestController
@RequestMapping("/api/appointment-change-request")
@Tag(name = "预约变更申请")
public class AppointmentChangeRequestController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentChangeRequestController.class);

    @Autowired
    private AppointmentChangeRequestService changeRequestService;

    @Autowired
    private AppointmentStoreGuard appointmentStoreGuard;

    /** 从登录态解析当前用户 ID（RoleInterceptor 注入）。写操作一律以登录态为准，禁止信任前端传入的 userId */
    private Long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /**
     * 用户提交变更申请（小程序端）
     * body: userId, appointmentType, appointmentId, requestedDate, requestedTimeSlot, requestedLocation, requestedRemark, requestedContactPhone
     */
    @PostMapping("/submit")
    @Operation(summary = "用户提交预约变更申请")
    public Result<AppointmentChangeRequest> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        // 安全修复：申请人以登录态为准，忽略 body 中的 userId（防止替他人提交变更申请）
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        if (appointmentType == null || appointmentId == null) {
            return Result.error("缺少 appointmentType 或 appointmentId");
        }
        LocalDate requestedDate = body.get("requestedDate") != null
                ? LocalDate.parse(body.get("requestedDate").toString())
                : null;
        String requestedTimeSlot = (String) body.get("requestedTimeSlot");
        String requestedLocation = (String) body.get("requestedLocation");
        String requestedRemark = (String) body.get("requestedRemark");
        String requestedContactPhone = (String) body.get("requestedContactPhone");
        String requestedKeyHandoverMethod = body.get("requestedKeyHandoverMethod") != null ? body.get("requestedKeyHandoverMethod").toString() : null;
        String requestedKeyReturnMethod = body.get("requestedKeyReturnMethod") != null ? body.get("requestedKeyReturnMethod").toString() : null;
        Long requestedPetId = body.get("requestedPetId") != null ? getLong(body, "requestedPetId") : null;
        try {
            AppointmentChangeRequest req = changeRequestService.submit(
                    userId, appointmentType, appointmentId,
                    requestedDate, requestedTimeSlot, requestedLocation, requestedRemark, requestedContactPhone,
                    requestedKeyHandoverMethod, requestedKeyReturnMethod, requestedPetId);
            return Result.success(req);
        } catch (IllegalArgumentException e) {
            log.error("操作失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 工作人员：待确认的变更申请列表
     */
    @GetMapping("/pending")
    @Operation(summary = "待确认的变更申请列表（后台）")
    public Result<List<AppointmentChangeRequest>> listPending(HttpServletRequest request) {
        // 安全修复：审批队列含所有用户的联系方式，仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查看变更申请列表");
        }
        List<AppointmentChangeRequest> list = changeRequestService.listPending();
        // 安全修复：店长/店员只能看到本门店预约的变更申请（申请含用户联系方式）
        Long scope = appointmentStoreGuard.scopedStoreId(request);
        if (scope != null) {
            list.removeIf(r -> !appointmentStoreGuard.canAccess(scope, r.getAppointmentType(), r.getAppointmentId()));
        }
        return Result.success(list);
    }

    /**
     * 工作人员：同意变更
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "同意变更")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：审批操作仅限 staff/admin，防止用户自行批准变更
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权审批变更申请");
        }
        // 安全修复：店长/店员只能审批本门店预约的变更申请
        if (!canHandlePending(request, id)) {
            return Result.error(403, "无权审批其他门店的变更申请");
        }
        boolean ok = changeRequestService.approve(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 工作人员：拒绝变更
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝变更")
    public Result<Void> reject(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：审批操作仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权审批变更申请");
        }
        // 安全修复：店长/店员只能审批本门店预约的变更申请
        if (!canHandlePending(request, id)) {
            return Result.error(403, "无权审批其他门店的变更申请");
        }
        boolean ok = changeRequestService.reject(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 查询某预约的待处理变更（用于前端展示「变更待确认」）
     */
    @GetMapping("/pending-by-appointment")
    @Operation(summary = "按预约查询待处理变更")
    public Result<AppointmentChangeRequest> getPendingByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId,
            HttpServletRequest request) {
        AppointmentChangeRequest req = changeRequestService.getPendingByAppointment(appointmentType, appointmentId);
        // 安全修复：申请含联系方式等个人信息——变更申请表不存 userId，需回查预约归属；普通用户仅能查看本人预约的申请
        if (req != null && !isStaffOrAdmin(request)) {
            Long userId = currentUserId(request);
            Long ownerId = changeRequestService.getAppointmentOwnerId(appointmentType, appointmentId);
            if (userId == null || !userId.equals(ownerId)) {
                return Result.success(null);
            }
        }
        return Result.success(req);
    }

    /**
     * 查询某预约是否已有任意变更记录（用于前端隐藏“申请变更”按钮）
     */
    @GetMapping("/has-any")
    @Operation(summary = "按预约查询是否已有变更记录")
    public Result<Boolean> hasAnyByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId) {
        boolean hasAny = changeRequestService.hasAnyChangeRequest(appointmentType, appointmentId);
        return Result.success(hasAny);
    }

    /**
     * 用户取消变更（仅工作人员未确认时可取消，预约状态改回已确认）
     */
    @PostMapping("/cancel")
    @Operation(summary = "用户取消变更申请")
    public Result<Void> cancelByUser(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        // 安全修复：操作人以登录态为准，忽略 body 中的 userId（防止替他人撤销变更申请）
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        if (appointmentType == null || appointmentId == null) {
            return Result.error("缺少 appointmentType 或 appointmentId");
        }
        try {
            boolean ok = changeRequestService.cancelByUser(appointmentType, appointmentId, userId);
            return ok ? Result.success(null) : Result.error("无待确认的变更申请或已处理");
        } catch (IllegalArgumentException e) {
            log.error("操作失败", e);
            return Result.error(e.getMessage());
        }
    }

    /** 待处理申请是否属于当前账号门店（平台管理员一律放行） */
    private boolean canHandlePending(HttpServletRequest request, Long requestId) {
        Long scope = appointmentStoreGuard.scopedStoreId(request);
        if (scope == null) {
            return true;
        }
        if (requestId == null) {
            return false;
        }
        List<AppointmentChangeRequest> pending = changeRequestService.listPending();
        for (AppointmentChangeRequest r : pending) {
            if (requestId.equals(r.getId())) {
                return appointmentStoreGuard.canAccess(scope, r.getAppointmentType(), r.getAppointmentId());
            }
        }
        return false;
    }

    private static Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
