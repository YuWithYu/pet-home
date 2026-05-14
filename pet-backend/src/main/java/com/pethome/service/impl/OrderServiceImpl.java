package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Notification;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import com.pethome.service.NotificationService;
import com.pethome.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired(required = false)
    private NotificationService notificationService;

    @Override
    public IPage<Order> getOrderList(Page<Order> page) {
        return getOrderList(page, null);
    }

    @Override
    public IPage<Order> getOrderList(Page<Order> page, String orderCategory) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.ne(Order::getStatus, -2);
        if ("mall".equals(orderCategory)) {
            qw.and(w -> w.isNull(Order::getOrderNo).or().notLike(Order::getOrderNo, "EX%"));
        } else if ("points".equals(orderCategory)) {
            qw.likeRight(Order::getOrderNo, "EX");
        }
        qw.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, qw);
    }

    @Override
    public IPage<Order> getOrderListForUser(Page<Order> page, Long userId) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getUserId, userId);
        qw.ne(Order::getStatus, -2);
        qw.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, qw);
    }

    @Override
    public Order createOrder(Order order) {
        orderMapper.insert(order);
        return order;
    }

    @Override
    public Order updateOrderStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            Integer oldStatus = order.getStatus();
            order.setStatus(status);
            if (status == 2) order.setDeliveryStatus(1);
            if (status == 3) order.setDeliveryStatus(2);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            if (notificationService != null && (oldStatus == null || !oldStatus.equals(status)) && order.getUserId() != null) {
                String title = null, content = null;
                String orderNo = order.getOrderNo() != null ? order.getOrderNo() : String.valueOf(id);
                if (status == 2) { title = "订单已发货"; content = "您的订单 " + orderNo + " 已发货，请留意物流信息。"; }
                else if (status == 3) { title = "订单已完成"; content = "您的订单 " + orderNo + " 已完成，感谢您的购买。"; }
                else if (status == -1) { title = "订单已取消"; content = "您的订单 " + orderNo + " 已取消。"; }
                if (title != null) {
                    Notification n = new Notification();
                    n.setUserId(order.getUserId()); n.setTitle(title); n.setContent(content);
                    n.setType("order_status"); n.setStatus(0); n.setRelatedId(id); n.setRelatedType("order");
                    n.setCreateTime(LocalDateTime.now()); n.setUpdateTime(LocalDateTime.now());
                    notificationService.createNotification(n);
                }
            }
        }
        return order;
    }

    @Override
    public Order updateOrder(Order order) {
        if (order != null && order.getId() != null) {
            // 更新更新时间
            order.setUpdateTime(java.time.LocalDateTime.now());
            orderMapper.updateById(order);
        }
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }
}
