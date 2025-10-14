package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import com.pethome.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public IPage<Order> getOrderList(Page<Order> page) {
        return orderMapper.selectPage(page, null);
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
            order.setStatus(status);
            orderMapper.updateById(order);
        }
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }
}
