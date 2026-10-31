package com.pethome.task;

import com.pethome.service.OrderMaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单自动确认收货调度入口：发货满 7 天仍未确认收货的订单置为已完成。
 */
@Component
public class OrderAutoCompleteTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCompleteTask.class);

    private final OrderMaintenanceService orderMaintenanceService;

    public OrderAutoCompleteTask(OrderMaintenanceService orderMaintenanceService) {
        this.orderMaintenanceService = orderMaintenanceService;
    }

    /**
     * 每天凌晨 2 点执行。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCompleteShippedOrders() {
        try {
            int affected = orderMaintenanceService.autoCompleteShippedOrders();
            if (affected > 0) {
                logger.info("订单自动确认收货任务完成: 共完成 {} 笔", affected);
            }
        } catch (Exception e) {
            logger.error("订单自动确认收货任务异常", e);
        }
    }
}
