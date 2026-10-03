package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单定时任务：发货满 7 天后若用户未确认收货，系统自动确认收货（状态改为已完成）
 *
 * 性能修复：原 N+1 单条调用改为批量 UPDATE
 */
@Component
public class OrderAutoCompleteTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCompleteTask.class);

    private static final int STATUS_SHIPPED = 2;   // 已发货
    private static final int STATUS_COMPLETED = 3; // 已完成
    private static final int AUTO_COMPLETE_DAYS = 7;
    private static final int BATCH_LIMIT = 500;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 每天凌晨 2 点执行：将「已发货」且发货时间距今 ≥ 7 天的订单批量改为「已完成」
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCompleteShippedOrders() {
        try {
            LocalDateTime deadline = LocalDateTime.now().minusDays(AUTO_COMPLETE_DAYS);

            UpdateWrapper<Order> uw = new UpdateWrapper<>();
            uw.eq("status", STATUS_SHIPPED)
              .isNotNull("shipping_time")
              .le("shipping_time", deadline)
              .set("status", STATUS_COMPLETED)
              .set("delivery_status", 2)
              .set("update_time", LocalDateTime.now())
              .last("LIMIT " + BATCH_LIMIT);
            int affected = orderMapper.update(null, uw);

            if (affected > 0) {
                logger.info("订单自动确认收货任务完成: 共完成 {} 笔（截止时间 {}）", affected, deadline);
            }
        } catch (Exception e) {
            logger.error("订单自动确认收货任务异常", e);
        }
    }
}
