package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Order;

public interface OrderService {
    IPage<Order> getOrderList(Page<Order> page);

    /**
     * 管理端订单列表
     * @param orderCategory null 或 all：全部；mall：普通商城（排除积分兑换单 EX 开头）；points：仅积分兑换单
     */
    IPage<Order> getOrderList(Page<Order> page, String orderCategory);

    /** C 端列表：按买家过滤并排除已删除，避免扫全表 */
    IPage<Order> getOrderListForUser(Page<Order> page, Long userId);
    Order createOrder(Order order);
    Order updateOrderStatus(Long id, Integer status);
    Order updateOrder(Order order);
    Order getOrderById(Long id);
    Order getOrderByOrderNo(String orderNo);
}
