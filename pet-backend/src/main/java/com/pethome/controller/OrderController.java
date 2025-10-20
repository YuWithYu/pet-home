package com.pethome.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
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
@Api(tags = "订单管理")
public class OrderController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 获取铲屎服务预约订单列表（兼容原有前端路径）
     */
    @GetMapping("/litter-appointments")
    @ApiOperation("获取铲屎服务预约订单列表")
    public Result<Map<String, Object>> getLitterAppointments(@RequestParam(required = false) String status) {
        try {
            Page<Appointment> page = new Page<>(1, 1000);
            IPage<Appointment> appointmentPage = appointmentService.getAppointmentList(page);
            List<Appointment> allAppointments = appointmentPage.getRecords();
            List<Map<String, Object>> orders = new ArrayList<>();
            for (Appointment appointment : allAppointments) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", "APPT_" + appointment.getId());
                order.put("customer", "用户" + appointment.getUserId());
                order.put("phone", appointment.getContactPhone() != null ? appointment.getContactPhone() : "");
                order.put("address", appointment.getLocation());
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
                order.put("createTime", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
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
            return Result.error("获取铲屎服务预约列表失败: " + e.getMessage());
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
                order.put("id", appointment.getId());
                order.put("customer", appointment.getContactName() != null ? appointment.getContactName() : ("用户" + appointment.getUserId()));
                order.put("phone", appointment.getContactPhone());
                order.put("address", appointment.getLocation());
                order.put("totalAmount", appointment.getPrice() != null ? appointment.getPrice() : BigDecimal.ZERO);
                // 直接透传数据库状态，前端已支持 pending/confirmed/completed/cancelled
                order.put("status", appointment.getStatus() != null ? appointment.getStatus() : "pending");
                String apptDateStr = "";
                if (appointment.getDate() != null) {
                    apptDateStr = appointment.getDate().toString();
                } else if (appointment.getAppointmentDate() != null) {
                    apptDateStr = appointment.getAppointmentDate().toString();
                }
                order.put("appointmentDate", apptDateStr);
                // 列表的“下单时间”显示预约日期，若无则退回创建时间
                order.put("createTime", !apptDateStr.isEmpty() ? apptDateStr : (appointment.getCreateTime() != null ? appointment.getCreateTime().toString() : ""));
                // 服务项目展示
                List<Map<String, Object>> products = new ArrayList<>();
                Map<String, Object> product = new HashMap<>();
                product.put("id", appointment.getId());
                product.put("name", appointment.getServiceType() != null ? appointment.getServiceType() : "上门铲屎服务");
                products.add(product);
                order.put("products", products);
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
        try {
            Long id = Long.parseLong(orderId);
            Appointment appt = appointmentService.getAppointmentById(id);
            if (appt == null) {
                return Result.error(404, "订单不存在");
            }
            Map<String, Object> order = new HashMap<>();
            order.put("id", appt.getId());
            order.put("customer", appt.getContactName() != null ? appt.getContactName() : ("用户" + appt.getUserId()));
            order.put("phone", appt.getContactPhone());
            order.put("address", appt.getLocation());
            order.put("totalAmount", appt.getPrice() != null ? appt.getPrice() : BigDecimal.ZERO);
            order.put("status", appt.getStatus());
            String apptDateStr = "";
            if (appt.getDate() != null) {
                apptDateStr = appt.getDate().toString();
            } else if (appt.getAppointmentDate() != null) {
                apptDateStr = appt.getAppointmentDate().toString();
            }
            order.put("appointmentDate", apptDateStr);
            order.put("createTime", !apptDateStr.isEmpty() ? apptDateStr : (appt.getCreateTime() != null ? appt.getCreateTime().toString() : ""));
            List<Map<String, Object>> products = new ArrayList<>();
            Map<String, Object> product = new HashMap<>();
            product.put("id", appt.getId());
            product.put("name", appt.getServiceType() != null ? appt.getServiceType() : "上门铲屎服务");
            products.add(product);
            order.put("products", products);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
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
