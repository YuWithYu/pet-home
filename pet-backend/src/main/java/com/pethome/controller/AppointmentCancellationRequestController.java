package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.AppointmentCancellationRequest;
import com.pethome.service.AppointmentCancellationRequestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 取消预约申请：用户提交取消原因，工作人员同意/拒绝；同意后执行取消并应用 0-2 小时违约金规则
 */
@RestController
@RequestMapping("/api/appointment-cancellation-request")
@Api(tags = "取消预约申请")
public class AppointmentCancellationRequestController {

    @Autowired
    private AppointmentCancellationRequestService cancellationRequestService;

    /**
     * 用户提交取消预约申请（小程序端，已确认订单需填原因）
     */
    @PostMapping("/submit")
    @ApiOperation("用户提交取消预约申请")
    public Result<AppointmentCancellationRequest> submit(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        String reason = body.get("reason") != null ? body.get("reason").toString().trim() : "";
        if (userId == null || appointmentType == null || appointmentId == null) {
            return Result.error("缺少 userId、appointmentType 或 appointmentId");
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
    @ApiOperation("待确认的取消预约申请列表（后台）")
    public Result<List<AppointmentCancellationRequest>> listPending() {
        List<AppointmentCancellationRequest> list = cancellationRequestService.listPending();
        return Result.success(list);
    }

    /**
     * 工作人员：同意取消（执行取消并应用 0-2 小时违约金规则）
     */
    @PostMapping("/{id}/approve")
    @ApiOperation("同意取消预约")
    public Result<Void> approve(@PathVariable Long id) {
        boolean ok = cancellationRequestService.approve(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 工作人员：拒绝取消
     */
    @PostMapping("/{id}/reject")
    @ApiOperation("拒绝取消预约")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
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
    @ApiOperation("按预约查询待处理取消申请")
    public Result<AppointmentCancellationRequest> getPendingByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId) {
        AppointmentCancellationRequest req = cancellationRequestService.getPendingByAppointment(appointmentType, appointmentId);
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
