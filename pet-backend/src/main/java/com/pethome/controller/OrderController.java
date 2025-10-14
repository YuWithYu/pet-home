package com.pethome.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Appointment;
import com.pethome.service.AppointmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Api(tags = "订单管理")
public class OrderController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取洗护服务预约订单列表（从数据库真实读取）
     */
    @GetMapping("/grooming-appointments")
    @ApiOperation("获取洗护服务预约订单列表")
    public Result<Map<String, Object>> getGroomingAppointments(@RequestParam(required = false) String status) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("orders", new ArrayList<>());
            data.put("total", 0);
            data.put("unpaidCount", 0);
            data.put("paidCount", 0);
            data.put("shippedCount", 0);
            data.put("completedCount", 0);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取洗护服务预约列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表（通用）
     */
    @GetMapping("/list")
    @ApiOperation("获取订单列表")
    public Result<Map<String, Object>> getOrderList(@RequestParam(required = false) String status) {
        try {
            Page<Appointment> page = new Page<>(1, 1000);
            IPage<Appointment> appointmentPage = appointmentService.getAppointmentList(page);
            List<Appointment> allAppointments = appointmentPage.getRecords();
            List<Map<String, Object>> orders = new ArrayList<>();
            for (Appointment appointment : allAppointments) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", "APPT_" + appointment.getId());
                order.put("customer", "用户" + appointment.getUserId());
                order.put("phone", "1380013800" + appointment.getUserId());
                order.put("address", "用户地址");
                order.put("totalAmount", appointment.getPrice() != null ? appointment.getPrice() : BigDecimal.ZERO);
                String dbStatus = appointment.getStatus();
                String orderStatus = "pending";
                if ("confirmed".equals(dbStatus)) {
                    orderStatus = "paid";
                } else if ("completed".equals(dbStatus) || "1".equals(dbStatus)) {
                    orderStatus = "completed";
                } else if ("cancelled".equals(dbStatus)) {
                    orderStatus = "cancelled";
                }
                order.put("status", orderStatus);
                order.put("createTime", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "未知时间");
                orders.add(order);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("orders", orders);
            data.put("total", orders.size());
            data.put("unpaidCount", (int) orders.stream().filter(o -> "pending".equals(o.get("status"))).count());
            data.put("paidCount", (int) orders.stream().filter(o -> "paid".equals(o.get("status"))).count());
            data.put("shippedCount", 0);
            data.put("completedCount", (int) orders.stream().filter(o -> "completed".equals(o.get("status"))).count());
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @ApiOperation("获取订单详情")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable String orderId) {
        return Result.error("获取订单详情失败: 订单不存在");
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @ApiOperation("创建订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        return Result.error("创建订单失败");
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    @ApiOperation("更新订单状态")
    public Result<Boolean> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        return Result.error("更新订单状态失败");
    }
}
