package com.pethome.controller.appointment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Booking;
import com.pethome.entity.TimeSlot;
import com.pethome.service.appointment.BookingService;
import com.pethome.service.common.RedisCacheService;
import com.pethome.service.appointment.TimeSlotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "预约管理")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private com.pethome.service.appointment.AppointmentDelayService appointmentDelayService;

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

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

    @GetMapping("/time-slots/available")
    @Operation(summary = "获取可用时间段")
    public Map<String, Object> getAvailableTimeSlots(
            @RequestParam String serviceType,
            @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询该服务类型的所有时间段配置
            QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
            wrapper.eq("service_type", serviceType);
            wrapper.eq("is_active", true);
            wrapper.orderByAsc("time_slot");
            
            List<TimeSlot> timeSlots = timeSlotService.list(wrapper);
            
            // 查询当天已有预约
            LocalDate bookingDate = LocalDate.parse(date);
            QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
            bookingWrapper.eq("service_type", serviceType);
            bookingWrapper.eq("booking_date", bookingDate);
            bookingWrapper.in("status", "pending", "confirmed");
            
            List<Booking> existingBookings = bookingService.list(bookingWrapper);
            
            // 统计每个时间段的预约数
            Map<String, Long> bookingCounts = new HashMap<>();
            for (Booking booking : existingBookings) {
                String timeSlot = booking.getTimeSlot();
                bookingCounts.put(timeSlot, bookingCounts.getOrDefault(timeSlot, 0L) + 1);
            }
            
            // 构建可用时间段列表
            List<Map<String, Object>> availableSlots = new ArrayList<>();
            LocalTime now = LocalDate.now().equals(bookingDate) ? LocalTime.now() : null;
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            for (TimeSlot slot : timeSlots) {
                String timeSlotStr = slot.getTimeSlot();
                if (now != null && timeSlotStr != null && !timeSlotStr.isEmpty()) {
                    int dash = timeSlotStr.indexOf('-');
                    String endStr = dash >= 0 ? timeSlotStr.substring(dash + 1).trim() : timeSlotStr.trim();
                    try {
                        LocalTime endTime = LocalTime.parse(endStr, timeFmt);
                        if (!endTime.isAfter(now)) {
                            continue;
                        }
                    } catch (DateTimeParseException ignored) {
                    }
                }
                Map<String, Object> slotInfo = new HashMap<>();
                slotInfo.put("time", timeSlotStr);
                Long currentBookings = bookingCounts.getOrDefault(timeSlotStr, 0L);
                boolean available = currentBookings < slot.getMaxBookings();
                slotInfo.put("available", available);
                slotInfo.put("remaining", slot.getMaxBookings() - currentBookings);
                availableSlots.add(slotInfo);
            }
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", availableSlots);
        } catch (Exception e) {
            log.error("获取时间段失败", e);
            result.put("code", -1);
            result.put("msg", "获取时间段失败，请稍后重试");
            result.put("data", null);
        }
        
        return result;
    }

    @PostMapping("/bookings/create")
    @Operation(summary = "创建预约")
    public Map<String, Object> createBooking(@RequestBody Booking booking,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 安全修复：归属强制以登录态为准，body 中的 userId 一律覆盖（杜绝替他人创建预约）
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                result.put("code", 401);
                result.put("msg", "未登录或登录已过期");
                return result;
            }
            booking.setUserId(currentUserId);

            // 验证时间段是否还有空位
            LocalDate bookingDate = booking.getBookingDate();
            String timeSlot = booking.getTimeSlot();
            String serviceType = booking.getServiceType();
            
            // 查询时间段配置
            QueryWrapper<TimeSlot> slotWrapper = new QueryWrapper<>();
            slotWrapper.eq("service_type", serviceType);
            slotWrapper.eq("time_slot", timeSlot);
            slotWrapper.eq("is_active", true);
            
            TimeSlot timeSlotConfig = timeSlotService.getOne(slotWrapper);
            if (timeSlotConfig == null) {
                result.put("code", -1);
                result.put("msg", "无效的时间段");
                return result;
            }
            
            // 统计当前时间段的预约数
            QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
            bookingWrapper.eq("service_type", serviceType);
            bookingWrapper.eq("booking_date", bookingDate);
            bookingWrapper.eq("time_slot", timeSlot);
            bookingWrapper.in("status", "pending", "confirmed");
            
            long count = bookingService.count(bookingWrapper);
            
            if (count >= timeSlotConfig.getMaxBookings()) {
                result.put("code", -1);
                result.put("msg", "该时间段预约已满，请选择其他时间");
                return result;
            }
            
            // 设置默认状态
            booking.setStatus("pending");
            
            // 保存预约
            boolean success = bookingService.save(booking);
            
            if (success) {
                java.time.LocalDateTime aptTime = bookingDate != null ? bookingDate.atStartOfDay() : null;
                appointmentDelayService.registerDelayKeys(serviceType, booking.getId(), aptTime, timeSlot);
                Long uid = booking.getUserId();
                String svc = serviceType != null ? serviceType.replace("-", "").replace("_", "") + "预约" : "服务预约";
                appointmentDelayService.registerReminderKeys(serviceType, booking.getId(), uid, aptTime, svc);
                result.put("code", 0);
                result.put("msg", "预约成功");
                result.put("data", booking);
            } else {
                result.put("code", -1);
                result.put("msg", "预约失败，请重试");
            }
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "预约失败，请稍后重试");
            log.error("创建预约失败", e);
        }
        
        return result;
    }

    @GetMapping("/bookings/my-list")
    @Operation(summary = "获取我的预约列表")
    public Map<String, Object> getMyBookings(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contactPhone,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 安全修复：列表归属强制以登录态为准；contactPhone 仅作为本人预约的附加筛选，
            // 防止「任意登录用户传他人手机号拉取他人预约」的隐私泄露
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                result.put("code", 401);
                result.put("msg", "未登录或登录已过期");
                return result;
            }

            QueryWrapper<Booking> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", currentUserId);

            if (contactPhone != null && !contactPhone.isEmpty()) {
                wrapper.eq("contact_phone", contactPhone);
            }
            
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            }
            
            wrapper.orderByDesc("created_at");
            
            Page<Booking> page = new Page<>(current, size);
            IPage<Booking> bookingPage = bookingService.page(page, wrapper);
            
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", bookingPage);
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("code", -1);
            result.put("msg", "查询失败，请稍后重试");
        }
        
        return result;
    }

    @PutMapping("/bookings/{id}/cancel")
    @Operation(summary = "取消预约")
    public Map<String, Object> cancelBooking(@PathVariable Long id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 安全修复：归属校验——仅预约本人可取消，防止任意登录用户按 id 取消他人预约
            Long currentUserId = currentUserId(authHeader);
            if (currentUserId == null) {
                result.put("code", 401);
                result.put("msg", "未登录或登录已过期");
                return result;
            }

            Booking booking = bookingService.getById(id);
            if (booking == null) {
                result.put("code", -1);
                result.put("msg", "预约不存在");
                return result;
            }
            if (!currentUserId.equals(booking.getUserId())) {
                log.warn("越权取消预约被拒绝: userId={}, bookingId={}, ownerId={}",
                        currentUserId, id, booking.getUserId());
                result.put("code", 403);
                result.put("msg", "无权取消此预约");
                return result;
            }

            // 检查是否可以取消（已取消、已完成、已失约的预约不能取消）
            String status = booking.getStatus();
            if ("cancelled".equals(status) || "completed".equals(status) || "no_show".equals(status)) {
                result.put("code", -1);
                result.put("msg", "该预约无法取消");
                return result;
            }

            booking.setStatus("cancelled");
            boolean success = bookingService.updateById(booking);

            if (success) {
                // 取消Redis延时任务key
                appointmentDelayService.cancelDelayKeys(booking.getServiceType(), booking.getId());

                // 释放时间段锁，允许其他用户预约
                if (redisCacheService != null && booking.getBookingDate() != null
                    && booking.getTimeSlot() != null && booking.getUserId() != null) {
                    String cacheKey = "timeslot:" + booking.getServiceType() + ":"
                        + booking.getBookingDate() + ":" + booking.getTimeSlot() + ":" + booking.getUserId();
                    redisCacheService.deleteCache(cacheKey);
                }

                result.put("code", 0);
                result.put("msg", "取消成功");
            } else {
                result.put("code", -1);
                result.put("msg", "取消失败");
            }
        } catch (Exception e) {
            log.error("取消失败", e);
            result.put("code", -1);
            result.put("msg", "取消失败，请稍后重试");
        }

        return result;
    }
}


