package com.pethome.mq;

import com.pethome.config.RabbitMqConfig;
import com.pethome.service.order.OrderService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单超时消费端。
 *
 * 可靠性设计：
 * 1. 手动 ACK：业务成功才 basicAck，进程崩溃/异常时消息不丢；
 * 2. 失败 basicNack(requeue=false) 进入死信队列，由补偿监听器按次数重回业务队列（最多 3 次）；
 * 3. 幂等：消费记录表 + 业务状态机双重保证（cancelPendingOrder 本身带状态条件更新）。
 */
@Component
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class OrderTimeoutConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutConsumer.class);

    private static final int MAX_RETRY = 3;

    @Autowired(required = false)
    private OrderService orderService;

    @Autowired
    private MqConsumeLogService consumeLogService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqConfig.ORDER_TIMEOUT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void onOrderTimeout(OrderTimeoutMessage msg, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String bizKey = "order:timeout:" + msg.getOrderId();
        try {
            if (consumeLogService.isConsumed(RabbitMqConfig.ORDER_TIMEOUT_QUEUE, bizKey)) {
                log.info("订单超时消息重复投递，跳过: orderId={}", msg.getOrderId());
                channel.basicAck(tag, false);
                return;
            }
            if (orderService == null) {
                channel.basicAck(tag, false);
                return;
            }

            boolean cancelled = orderService.cancelPendingOrder(msg.getOrderId());
            consumeLogService.markConsumed(RabbitMqConfig.ORDER_TIMEOUT_QUEUE, bizKey);
            channel.basicAck(tag, false);
            log.info("订单超时处理完成: orderId={}, orderNo={}, cancelled={}",
                    msg.getOrderId(), msg.getOrderNo(), cancelled);
        } catch (Exception e) {
            log.error("订单超时处理失败，转入死信重试: orderId={}", msg.getOrderId(), e);
            channel.basicNack(tag, false, false);
        }
    }

    /**
     * 死信补偿：未超过重试上限则重新投递回业务队列，超限则告警并落地。
     */
    @RabbitListener(queues = RabbitMqConfig.ORDER_TIMEOUT_DLQ,
            containerFactory = "rabbitListenerContainerFactory")
    public void onDeadLetter(OrderTimeoutMessage msg, Message message, Channel channel,
                             @Header(value = RabbitMqConfig.HEADER_RETRY_COUNT, required = false) Integer retryCount)
            throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        int current = retryCount == null ? 0 : retryCount;
        if (current >= MAX_RETRY) {
            log.error("订单超时消息重试耗尽，需人工介入: orderId={}, orderNo={}, retry={}",
                    msg.getOrderId(), msg.getOrderNo(), current);
            channel.basicAck(tag, false);
            return;
        }
        try {
            int next = current + 1;
            rabbitTemplate.convertAndSend(RabbitMqConfig.BIZ_EXCHANGE, RabbitMqConfig.RK_ORDER_TIMEOUT, msg,
                    m -> {
                        m.getMessageProperties().setHeader(RabbitMqConfig.HEADER_RETRY_COUNT, next);
                        return m;
                    });
            channel.basicAck(tag, false);
            log.warn("订单超时消息重新入队补偿: orderId={}, retry={}", msg.getOrderId(), next);
        } catch (Exception e) {
            log.error("死信补偿重新投递失败: orderId={}", msg.getOrderId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
