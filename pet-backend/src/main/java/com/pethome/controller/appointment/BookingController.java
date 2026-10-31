package com.pethome.controller.appointment;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.Booking;
import com.pethome.service.appointment.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "预约管理")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired(required = false)
    private com.pethome.security.UserTokenResolver userTokenResolver;

    /**
     * 从 Authorization 头解析当前登录用户 ID（归属校验统一入口）。
     * Booking 的 userId/手机号等身份信息一律以此为准，禁止信任请求体/查询参数。
     */
    private Long currentUserId(String authHeader) {
        if (userTokenResolver == null || authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        String t = authHeader.trim();
        if (!t.startsWith("Bearer ")) {
            t = "Bearer " + t;
        }
        return userTokenResolver.getUserIdFromBearer(t);
    }

    /**
     * 该组接口历史返回体为 {code, msg, data}（成功 code=0、失败 code=-1），
     * 这里把统一 Result 适配回原契约，避免前端改动。
     */
    private static Map<String, Object> legacyBody(Result<?> result) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", result.getCode() != null && result.getCode() == 200 ? 0 : -1);
        body.put("msg", result.getMsg());
        body.put("data", result.getData());
        return body;
    }

    @GetMapping("/time-slots/available")
    @Operation(summary = "获取可用时间段")
    public Map<String, Object> getAvailableTimeSlots(
            @RequestParam String serviceType,
            @RequestParam String date) {
        return legacyBody(SafeCall.of(
                () -> Result.success(bookingService.getAvailableTimeSlots(serviceType, date)),
                "获取时间段失败，请稍后重试", log));
    }

    @PostMapping("/bookings/create")
    @Operation(summary = "创建预约")
    public Map<String, Object> createBooking(@RequestBody Booking booking,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return legacyBody(SafeCall.of(() -> {
            // 安全修复：归属强制以登录态为准，body 中的 userId 一律覆盖（杜绝替他人创建预约）
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }
            Booking created = bookingService.createBooking(booking, currentUserId);
            return Result.success("预约成功", created);
        }, "预约失败，请稍后重试", log));
    }

    @GetMapping("/bookings/my-list")
    @Operation(summary = "获取我的预约列表")
    public Map<String, Object> getMyBookings(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contactPhone,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return legacyBody(SafeCall.of(() -> {
            // 安全修复：列表归属强制以登录态为准；contactPhone 仅作为本人预约的附加筛选，
            // 防止「任意登录用户传他人手机号拉取他人预约」的隐私泄露
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }
            return Result.success(bookingService.getUserBookingPage(currentUserId, current, size, status, contactPhone));
        }, "查询失败，请稍后重试", log));
    }

    @PutMapping("/bookings/{id}/cancel")
    @Operation(summary = "取消预约")
    public Map<String, Object> cancelBooking(@PathVariable Long id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return legacyBody(SafeCall.of(() -> {
            // 安全修复：归属校验——仅预约本人可取消，防止任意登录用户按 id 取消他人预约
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }
            bookingService.cancelBooking(id, currentUserId);
            return Result.success("取消成功", null);
        }, "取消失败，请稍后重试", log));
    }
}
