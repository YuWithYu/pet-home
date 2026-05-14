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
 * 订单定时任务：待付款订单超过 30 分钟未支付则自动取消
 */
@Component
public class OrderAutoCancelTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCancelTask.class);

    private static final int STATUS_PENDING = 0;   // 待付款
    private static final int STATUS_CANCELLED = -1; // 已取消
    private static final int AUTO_CANCEL_MINUTES = 30;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 兜底任务：每 1 小时执行一次（主力已改为 Redis 时间驱动，见 AppointmentDelayService）
     * 将「待付款」且创建时间距今 ≥ 30 分钟的订单自动改为「已取消」
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoCancelUnpaidOrders() {
        try {
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(AUTO_CANCEL_MINUTES);
            QueryWrapper<Order> q = new QueryWrapper<>();
            q.eq("status", STATUS_PENDING)
             .le("create_time", deadline);
            List<Order> list = orderMapper.selectList(q);
            if (list == null || list.isEmpty()) {
                return;
            }
            int count = 0;
            for (Order order : list) {
                try {
                    orderService.updateOrderStatus(order.getId(), STATUS_CANCELLED);
                    count++;
                    logger.info("订单超时自动取消: orderNo={}, 创建时间={}", order.getOrderNo(), order.getCreateTime());
                } catch (Exception e) {
                    logger.warn("订单自动取消失败: orderId={}, {}", order.getId(), e.getMessage());
                }
            }
            if (count > 0) {
                logger.info("订单超时自动取消任务完成: 共 {} 笔", count);
            }
        } catch (Exception e) {
            logger.error("订单超时自动取消任务异常", e);
        }
    }
}
