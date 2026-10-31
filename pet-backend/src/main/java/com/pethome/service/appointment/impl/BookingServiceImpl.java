package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.common.BusinessException;
import com.pethome.entity.Booking;
import com.pethome.entity.TimeSlot;
import com.pethome.mapper.BookingMapper;
import com.pethome.service.appointment.AppointmentDelayService;
import com.pethome.service.appointment.BookingService;
import com.pethome.service.appointment.TimeSlotService;
import com.pethome.service.common.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private AppointmentDelayService appointmentDelayService;

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(String serviceType, String date) {
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

        List<Booking> existingBookings = this.list(bookingWrapper);

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
        return availableSlots;
    }

    @Override
    public Booking createBooking(Booking booking, Long userId) {
        booking.setUserId(userId);

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
            throw new BusinessException(-1, "无效的时间段");
        }

        // 统计当前时间段的预约数
        QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
        bookingWrapper.eq("service_type", serviceType);
        bookingWrapper.eq("booking_date", bookingDate);
        bookingWrapper.eq("time_slot", timeSlot);
        bookingWrapper.in("status", "pending", "confirmed");

        long count = this.count(bookingWrapper);
        if (count >= timeSlotConfig.getMaxBookings()) {
            throw new BusinessException(-1, "该时间段预约已满，请选择其他时间");
        }

        booking.setStatus("pending");
        if (!this.save(booking)) {
            throw new BusinessException(-1, "预约失败，请重试");
        }

        LocalDateTime aptTime = bookingDate != null ? bookingDate.atStartOfDay() : null;
        appointmentDelayService.registerDelayKeys(serviceType, booking.getId(), aptTime, timeSlot);
        String svc = serviceType != null ? serviceType.replace("-", "").replace("_", "") + "预约" : "服务预约";
        appointmentDelayService.registerReminderKeys(serviceType, booking.getId(), booking.getUserId(), aptTime, svc);
        return booking;
    }

    @Override
    public IPage<Booking> getUserBookingPage(Long userId, Integer current, Integer size, String status, String contactPhone) {
        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        if (contactPhone != null && !contactPhone.isEmpty()) {
            wrapper.eq("contact_phone", contactPhone);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("created_at");
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    public Booking cancelBooking(Long bookingId, Long userId) {
        Booking booking = this.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(-1, "预约不存在");
        }
        if (!userId.equals(booking.getUserId())) {
            log.warn("越权取消预约被拒绝: userId={}, bookingId={}, ownerId={}", userId, bookingId, booking.getUserId());
            throw new BusinessException(403, "无权取消此预约");
        }

        // 已取消、已完成、已失约的预约不能取消
        String status = booking.getStatus();
        if ("cancelled".equals(status) || "completed".equals(status) || "no_show".equals(status)) {
            throw new BusinessException(-1, "该预约无法取消");
        }

        booking.setStatus("cancelled");
        if (!this.updateById(booking)) {
            throw new BusinessException(-1, "取消失败");
        }

        // 取消 Redis 延时任务 key
        appointmentDelayService.cancelDelayKeys(booking.getServiceType(), booking.getId());

        // 释放时间段锁，允许其他用户预约
        if (redisCacheService != null && booking.getBookingDate() != null
                && booking.getTimeSlot() != null && booking.getUserId() != null) {
            String cacheKey = "timeslot:" + booking.getServiceType() + ":"
                    + booking.getBookingDate() + ":" + booking.getTimeSlot() + ":" + booking.getUserId();
            redisCacheService.deleteCache(cacheKey);
        }
        return booking;
    }
}
