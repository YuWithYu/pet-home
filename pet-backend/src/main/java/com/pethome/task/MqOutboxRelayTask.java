package com.pethome.task;

import com.pethome.common.lock.RedisDistributedLock;
import com.pethome.mq.MqOutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 本地消息表补偿任务：每分钟扫描一次待投递消息。
 * 应用停机期间积压的消息在启动后 1 分钟内即可恢复投递。
 * 多实例部署时用 Redis 分布式锁保证同一轮只有一台机器执行，避免重复投递。
 */
@Component
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class MqOutboxRelayTask {

    private static final Logger log = LoggerFactory.getLogger(MqOutboxRelayTask.class);

    private static final String TASK_LOCK_KEY = "task:lock:mq-outbox-relay";

    @Autowired
    private MqOutboxService mqOutboxService;

    @Autowired
    private RedisDistributedLock distributedLock;

    @Scheduled(fixedDelay = 60_000L, initialDelay = 10_000L)
    public void relayPendingMessages() {
        distributedLock.tryRun(TASK_LOCK_KEY, Duration.ofSeconds(55), () -> {
            try {
                mqOutboxService.publishPending();
            } catch (Exception e) {
                log.error("本地消息表补偿任务异常", e);
            }
        });
    }
}
