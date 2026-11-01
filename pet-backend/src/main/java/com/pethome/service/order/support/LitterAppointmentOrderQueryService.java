package com.pethome.service.order.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Appointment;
import com.pethome.service.appointment.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上门服务（铲屎）预约的订单视角查询组件。
 * <p>
 * 把预约数据投影成订单列表结构，供订单管理页统一展示，避免与商品订单查询逻辑耦合。
 */
@Service
public class LitterAppointmentOrderQueryService {

    private static final int MAX_PAGE_SIZE = 1000;

    @Autowired(required = false)
    private AppointmentService appointmentService;

    public Map<String, Object> getLitterAppointments(String status, boolean adminSide, Long currentUserId) {
        if (!adminSide && currentUserId == null) {
            throw new BusinessException(401, "未登录或token无效");
        }
        List<Appointment> allAppointments = adminSide
                ? appointmentService.getAppointmentList(new Page<>(1, MAX_PAGE_SIZE)).getRecords()
                : appointmentService.getUserAppointments(currentUserId);
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
        return data;
    }
}
