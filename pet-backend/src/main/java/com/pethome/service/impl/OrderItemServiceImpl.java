package com.pethome.service.impl;

import com.pethome.entity.OrderItem;
import com.pethome.mapper.OrderItemMapper;
import com.pethome.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public Map<Long, List<OrderItem>> mapOrderItemsByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<OrderItem> rows = orderItemMapper.selectByOrderIds(orderIds);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        return rows.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    @Override
    public void saveOrderItem(OrderItem orderItem) {
        orderItemMapper.insert(orderItem);
    }

    @Override
    public void saveOrderItems(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            orderItemMapper.insert(orderItem);
        }
    }
}
