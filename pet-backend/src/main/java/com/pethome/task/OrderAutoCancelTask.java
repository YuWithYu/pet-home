package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.lock.RedisDistributedLock;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import com.pethome.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务：待付款订单超过 30 分钟未支付则自动取消
 *
 * 设计说明：
 * 1. 取消统一走 OrderService.cancelPendingOrder（状态机 + 库存恢复 + 优惠券返还 + 通知），
 *    历史版本曾用单条批量 UPDATE 绕过 Service，导致超时取消的库存被永久锁死（不恢复）。
 * 2. 先批量查 ID（LIMIT 控制单次处理量，避免长事务），再逐笔走 Service；
 *    每笔独立事务，单笔失败不影响批次其余订单。
 */
@Component
public class OrderAutoCancelTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderAutoCancelTask.class);

    private static final int STATUS_PENDING = 0;   // 待付款
    private static final int STATUS_CANCELLED = -1; // 已取消
    private static final int AUTO_CANCEL_MINUTES = 30;
    /** 单次处理上限，避免订单积压时单任务处理量过大 */
    private static final int BATCH_LIMIT = 500;
    /** 定时任务的分布式锁 key */
    private static final String TASK_LOCK_KEY = "task:lock:order-auto-cancel";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisDistributedLock distributedLock;

    @Autowired(required = false)
    private OrderService orderService;

    /**
     * 兜底任务：每 1 小时执行一次（主力已改为 Redis 时间驱动）
     * 将「待付款」且创建时间距今 ≥ 30 分钟的订单逐笔取消（含库存恢复）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoCancelUnpaidOrders() {
        // 多实例部署：分布式锁保证同一时刻仅一个实例执行（ttl 覆盖单批处理耗时）
        distributedLock.tryRun(TASK_LOCK_KEY, Duration.ofMinutes(10), this::cancelUnpaidOrdersBatch);
    }

    private void cancelUnpaidOrdersBatch() {
        try {
            if (orderService == null) {
                logger.warn("OrderService 未注入，跳过本次订单超时取消");
                return;
            }
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(AUTO_CANCEL_MINUTES);

            // 批量查超时订单 ID（LIMIT 防止单次处理量过大）
            QueryWrapper<Order> qw = new QueryWrapper<>();
            qw.select("id")
              .eq("status", STATUS_PENDING)
              .le("create_time", deadline)
              .last("LIMIT " + BATCH_LIMIT);
            List<Object> ids = orderMapper.selectObjs(qw);

            int cancelled = 0;
            for (Object idObj : ids) {
                Long id = ((Number) idObj).longValue();
                try {
                    if (orderService.cancelPendingOrder(id)) {
                        cancelled++;
                    }
                } catch (Exception e) {
                    // 单笔失败不阻塞批次（状态竞争/网络抖动等，下次任务兜底重试）
                    logger.warn("订单超时取消失败: orderId={}, err={}", id, e.getMessage());
                }
            }

            if (cancelled > 0) {
                logger.info("订单超时自动取消任务完成: 共取消 {} 笔（截止时间 {}）", cancelled, deadline);
            }
        } catch (Exception e) {
            logger.error("订单超时自动取消任务异常", e);
        }
    }
}
