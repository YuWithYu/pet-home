package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.AppointmentChangeRequest;
import com.pethome.service.AppointmentChangeRequestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约变更申请：用户提交变更；工作人员查询待处理、同意/拒绝
 */
@RestController
@RequestMapping("/api/appointment-change-request")
@Api(tags = "预约变更申请")
public class AppointmentChangeRequestController {

    @Autowired
    private AppointmentChangeRequestService changeRequestService;

    /**
     * 用户提交变更申请（小程序端）
     * body: userId, appointmentType, appointmentId, requestedDate, requestedTimeSlot, requestedLocation, requestedRemark, requestedContactPhone
     */
    @PostMapping("/submit")
    @ApiOperation("用户提交预约变更申请")
    public Result<AppointmentChangeRequest> submit(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        if (userId == null || appointmentType == null || appointmentId == null) {
            return Result.error("缺少 userId、appointmentType 或 appointmentId");
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
            return Result.error(e.getMessage());
        }
    }

    /**
     * 工作人员：待确认的变更申请列表
     */
    @GetMapping("/pending")
    @ApiOperation("待确认的变更申请列表（后台）")
    public Result<List<AppointmentChangeRequest>> listPending(HttpServletRequest request) {
        List<AppointmentChangeRequest> list = changeRequestService.listPending();
        return Result.success(list);
    }

    /**
     * 工作人员：同意变更
     */
    @PostMapping("/{id}/approve")
    @ApiOperation("同意变更")
    public Result<Void> approve(@PathVariable Long id) {
        boolean ok = changeRequestService.approve(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 工作人员：拒绝变更
     */
    @PostMapping("/{id}/reject")
    @ApiOperation("拒绝变更")
    public Result<Void> reject(@PathVariable Long id) {
        boolean ok = changeRequestService.reject(id);
        return ok ? Result.success(null) : Result.error("操作失败或该申请已处理");
    }

    /**
     * 查询某预约的待处理变更（用于前端展示「变更待确认」）
     */
    @GetMapping("/pending-by-appointment")
    @ApiOperation("按预约查询待处理变更")
    public Result<AppointmentChangeRequest> getPendingByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId) {
        AppointmentChangeRequest req = changeRequestService.getPendingByAppointment(appointmentType, appointmentId);
        return Result.success(req);
    }

    /**
     * 查询某预约是否已有任意变更记录（用于前端隐藏“申请变更”按钮）
     */
    @GetMapping("/has-any")
    @ApiOperation("按预约查询是否已有变更记录")
    public Result<Boolean> hasAnyByAppointment(
            @RequestParam String appointmentType, @RequestParam Long appointmentId) {
        boolean hasAny = changeRequestService.hasAnyChangeRequest(appointmentType, appointmentId);
        return Result.success(hasAny);
    }

    /**
     * 用户取消变更（仅工作人员未确认时可取消，预约状态改回已确认）
     */
    @PostMapping("/cancel")
    @ApiOperation("用户取消变更申请")
    public Result<Void> cancelByUser(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        String appointmentType = (String) body.get("appointmentType");
        Long appointmentId = getLong(body, "appointmentId");
        if (userId == null || appointmentType == null || appointmentId == null) {
            return Result.error("缺少 userId、appointmentType 或 appointmentId");
        }
        try {
            boolean ok = changeRequestService.cancelByUser(appointmentType, appointmentId, userId);
            return ok ? Result.success(null) : Result.error("无待确认的变更申请或已处理");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
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
