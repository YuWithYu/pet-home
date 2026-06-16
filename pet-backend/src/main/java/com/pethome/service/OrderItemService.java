package com.pethome.service;

import com.pethome.entity.OrderItem;
import java.util.List;
import java.util.Map;

public interface OrderItemService {
    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    /** 批量查询并按 orderId 分组，避免列表接口 N+1 */
    Map<Long, List<OrderItem>> mapOrderItemsByOrderIds(List<Long> orderIds);
    void saveOrderItem(OrderItem orderItem);
    void saveOrderItems(List<OrderItem> orderItems);
}
