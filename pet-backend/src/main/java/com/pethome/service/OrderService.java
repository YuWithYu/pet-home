package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Order;

public interface OrderService {
    IPage<Order> getOrderList(Page<Order> page);
    Order createOrder(Order order);
    Order updateOrderStatus(Long id, Integer status);
    Order getOrderById(Long id);
}
