package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.AppointmentCancellationRequest;
import com.pethome.service.AppointmentCancellationRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 取消预约申请：用户提交取消原因，工作人员同意/拒绝；同意后执行取消并应用 0-2 小时违约金规则
 */
@RestController
@RequestMapping("/api/appointment-cancellation-request")
@Tag(name = "取消预约申请")
public class AppointmentCancellationRequestController {

    @Autowired
    private AppointmentCancellationRequestService cancellationRequestService;

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
     * 用户提交取消预约申请（小程序端，已确认订单需填原因）
     */
    @PostMapping("/submit")
    @Operation(summary = "用户提交取消预约申请")
    public Result<AppointmentCancellationRequest> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        // 安全修复：申请人以登录态为准，忽略 body 中的 userId（防止替他人提交取消申请）
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        String reason = body.get("reason") != null ? body.get("reason").toString().trim() : "";
        if (appointmentType == null || appointmentId == null) {
            return Result.error("缺少 appointmentType 或 appointmentId");
        }
        try {
            AppointmentCancellationRequest req = cancellationRequestService.submit(userId, appointmentType, appointmentId, reason);
            return Result.success(req);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 工作人员：待确认的取消申请列表
     */
    @GetMapping("/pending")
    @Operation(summary = "待确认的取消预约申请列表（后台）")
    public Result<List<AppointmentCancellationRequest>> listPending(HttpServletRequest request) {
        // 安全修复：审批队列含所有用户的联系方式，仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查看取消申请列表");
        }
        List<AppointmentCancellationRequest> list = cancellationRequestService.listPending();
        return Result.success(list);
    }

    /**
     * 工作人员：同意取消（执行取消并应用 0-2 小时违约金规则）
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "同意取消预约")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：审批操作仅限 staff/admin，防止用户自行批准取消（绕过违约金规则）
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权审批取消申请");
        }
        boolean ok = cancellationRequestService.approve(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 工作人员：拒绝取消
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝取消预约")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body,
                               HttpServletRequest request) {
        // 安全修复：审批操作仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权审批取消申请");
        }
        String rejectReason = body != null && body.get("rejectReason") != null
                ? body.get("rejectReason").toString().trim()
                : "";
        try {
            boolean ok = cancellationRequestService.reject(id, rejectReason);
            return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 按预约查询待处理取消申请（用于前端展示「取消待确认」）
     */
    @GetMapping("/pending-by-appointment")
    @Operation(summary = "按预约查询待处理取消申请")
    public Result<AppointmentCancellationRequest> getPendingByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId,
            HttpServletRequest request) {
        AppointmentCancellationRequest req = cancellationRequestService.getPendingByAppointment(appointmentType, appointmentId);
        // 安全修复：申请含联系方式等个人信息，普通用户仅能查看本人申请（staff/admin 放行）
        if (req != null && !isStaffOrAdmin(request)) {
            Long userId = currentUserId(request);
            if (userId == null || !userId.equals(req.getUserId())) {
                return Result.success(null);
            }
        }
        return Result.success(req);
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
