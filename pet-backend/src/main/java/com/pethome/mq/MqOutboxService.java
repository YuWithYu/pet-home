package com.pethome.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.config.RabbitMqConfig;
import com.pethome.entity.MqOutbox;
import com.pethome.entity.Order;
import com.pethome.mapper.MqOutboxMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表（Transactional Outbox）投递服务。
 *
 * 解决的问题：订单落库成功但 MQ 投递失败 → 订单永远不会自动取消。
 * 做法：业务事务内写 mq_outbox（同库同事务，天然原子），独立补偿任务扫描待投递记录并重试，
 * 失败按指数退避重试，超过上限标记 status=2 并告警，保证「本地事务 + 消息」最终一致。
 */
@Service
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class MqOutboxService {

    private static final Logger log = LoggerFactory.getLogger(MqOutboxService.class);

    private static final int BATCH_SIZE = 50;

    @Autowired
    private MqOutboxMapper outboxMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.mq.outbox-max-retry:6}")
    private int maxRetry;

    /** 订单超时消息入队（与订单落库同一事务） */
    public void enqueueOrderTimeout(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }
        OrderTimeoutMessage payload = new OrderTimeoutMessage(
                order.getId(), order.getOrderNo(), order.getUserId(), System.currentTimeMillis());
        enqueue("order:timeout:" + order.getOrderNo(),
                RabbitMqConfig.DELAY_EXCHANGE, RabbitMqConfig.RK_ORDER_TIMEOUT_DELAY, payload);
    }

    /**
     * 写入本地消息表。biz_key 唯一索引保证同一业务只入队一次（幂等）。
     */
    public void enqueue(String bizKey, String exchange, String routingKey, Object payload) {
        try {
            MqOutbox row = new MqOutbox();
            row.setBizKey(bizKey);
            row.setExchangeName(exchange);
            row.setRoutingKey(routingKey);
            row.setPayload(objectMapper.writeValueAsString(payload));
            row.setStatus(0);
            row.setRetryCount(0);
            row.setNextRetryTime(LocalDateTime.now());
            row.setCreateTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            outboxMapper.insert(row);
        } catch (DuplicateKeyException e) {
            log.debug("消息重复入队，已忽略: bizKey={}", bizKey);
        } catch (Exception e) {
            log.error("本地消息表写入失败: bizKey={}", bizKey, e);
        }
    }

    /**
     * 补偿投递：扫描到期的待投递记录并发送。
     *
     * @return 本轮成功投递数量
     */
    public int publishPending() {
        QueryWrapper<MqOutbox> qw = new QueryWrapper<>();
        qw.eq("status", 0)
          .le("next_retry_time", LocalDateTime.now())
          .orderByAsc("id")
          .last("LIMIT " + BATCH_SIZE);
        List<MqOutbox> rows = outboxMapper.selectList(qw);
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        int success = 0;
        for (MqOutbox row : rows) {
            try {
                send(row);
                markSent(row.getId());
                success++;
            } catch (Exception e) {
                markFailed(row, e);
            }
        }
        if (success > 0) {
            log.info("本地消息表补偿投递完成: success={}, batch={}", success, rows.size());
        }
        return success;
    }

    /** 以原始 JSON 字节发送，避免经消息转换器二次编码成字符串 */
    private void send(MqOutbox row) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding("UTF-8");
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setHeader(RabbitMqConfig.HEADER_RETRY_COUNT, 0);
        Message message = new Message(row.getPayload().getBytes(StandardCharsets.UTF_8), props);
        rabbitTemplate.send(row.getExchangeName(), row.getRoutingKey(), message);
    }

    private void markSent(Long id) {
        MqOutbox upd = new MqOutbox();
        upd.setId(id);
        upd.setStatus(1);
        upd.setUpdateTime(LocalDateTime.now());
        outboxMapper.updateById(upd);
    }

    private void markFailed(MqOutbox row, Exception e) {
        int retry = (row.getRetryCount() == null ? 0 : row.getRetryCount()) + 1;
        MqOutbox upd = new MqOutbox();
        upd.setId(row.getId());
        upd.setRetryCount(retry);
        upd.setStatus(retry >= maxRetry ? 2 : 0);
        // 指数退避，上限 60 分钟
        upd.setNextRetryTime(LocalDateTime.now().plusMinutes(Math.min(1L << Math.min(retry, 6), 60L)));
        String err = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        upd.setLastError(err.length() > 500 ? err.substring(0, 500) : err);
        upd.setUpdateTime(LocalDateTime.now());
        outboxMapper.updateById(upd);
        log.warn("消息投递失败(第 {} 次): bizKey={}, err={}", retry, row.getBizKey(), upd.getLastError());
    }
}
