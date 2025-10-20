package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Booking;
import com.pethome.entity.TimeSlot;
import com.pethome.service.BookingService;
import com.pethome.service.TimeSlotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Api(tags = "预约管理")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/time-slots/available")
    @ApiOperation("获取可用时间段")
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
            for (TimeSlot slot : timeSlots) {
                Map<String, Object> slotInfo = new HashMap<>();
                slotInfo.put("time", slot.getTimeSlot());
                
                Long currentBookings = bookingCounts.getOrDefault(slot.getTimeSlot(), 0L);
                boolean available = currentBookings < slot.getMaxBookings();
                
                slotInfo.put("available", available);
                slotInfo.put("remaining", slot.getMaxBookings() - currentBookings);
                
                availableSlots.add(slotInfo);
            }
            
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", availableSlots);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "获取时间段失败: " + e.getMessage());
            result.put("data", null);
        }
        
        return result;
    }

    @PostMapping("/bookings/create")
    @ApiOperation("创建预约")
    public Map<String, Object> createBooking(@RequestBody Booking booking) {
        Map<String, Object> result = new HashMap<>();
        
        try {
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
                result.put("code", 0);
                result.put("msg", "预约成功");
                result.put("data", booking);
            } else {
                result.put("code", -1);
                result.put("msg", "预约失败，请重试");
            }
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "预约失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @GetMapping("/bookings/my-list")
    @ApiOperation("获取我的预约列表")
    public Map<String, Object> getMyBookings(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contactPhone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            QueryWrapper<Booking> wrapper = new QueryWrapper<>();
            
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
            result.put("code", -1);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    @PutMapping("/bookings/{id}/cancel")
    @ApiOperation("取消预约")
    public Map<String, Object> cancelBooking(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingService.getById(id);
            if (booking == null) {
                result.put("code", -1);
                result.put("msg", "预约不存在");
                return result;
            }
            
            if ("cancelled".equals(booking.getStatus()) || "completed".equals(booking.getStatus())) {
                result.put("code", -1);
                result.put("msg", "该预约无法取消");
                return result;
            }
            
            booking.setStatus("cancelled");
            boolean success = bookingService.updateById(booking);
            
            if (success) {
                result.put("code", 0);
                result.put("msg", "取消成功");
            } else {
                result.put("code", -1);
                result.put("msg", "取消失败");
            }
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "取消失败: " + e.getMessage());
        }
        
        return result;
    }
}

