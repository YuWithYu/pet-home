package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import com.pethome.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务：发货满 7 天后若用户未确认收货，系统自动确认收货（状态改为已完成）
 */
@Component
public class OrderAutoCompleteTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCompleteTask.class);

    private static final int STATUS_SHIPPED = 2;   // 已发货
    private static final int STATUS_COMPLETED = 3; // 已完成
    private static final int AUTO_COMPLETE_DAYS = 7;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 每天凌晨 2 点执行：将「已发货」且发货时间距今 ≥ 7 天的订单自动改为「已完成」
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCompleteShippedOrders() {
        try {
            LocalDateTime deadline = LocalDateTime.now().minusDays(AUTO_COMPLETE_DAYS);
            QueryWrapper<Order> q = new QueryWrapper<>();
            q.eq("status", STATUS_SHIPPED)
             .isNotNull("shipping_time")
             .le("shipping_time", deadline);
            List<Order> list = orderMapper.selectList(q);
            if (list == null || list.isEmpty()) {
                return;
            }
            int count = 0;
            for (Order order : list) {
                try {
                    orderService.updateOrderStatus(order.getId(), STATUS_COMPLETED);
                    count++;
                    logger.info("订单自动确认收货: orderNo={}, 发货时间={}", order.getOrderNo(), order.getShippingTime());
                } catch (Exception e) {
                    logger.warn("订单自动确认收货失败: orderId={}, {}", order.getId(), e.getMessage());
                }
            }
            if (count > 0) {
                logger.info("订单自动确认收货任务完成: 共 {} 笔", count);
            }
        } catch (Exception e) {
            logger.error("订单自动确认收货任务异常", e);
        }
    }
}
