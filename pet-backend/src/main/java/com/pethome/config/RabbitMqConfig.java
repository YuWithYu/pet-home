package com.pethome.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑配置（仅在 app.mq.enabled=true 时生效）。
 *
 * 订单超时链路采用「队列级 TTL + 死信路由」，不依赖 rabbitmq_delayed_message_exchange 插件：
 *
 *   order 创建 ──► pethome.delay.exchange ──► pethome.delay.order.queue（TTL 30min）
 *                                                   │ 到期成为死信
 *                                                   ▼
 *   pethome.biz.exchange ◄── 死信重投 ──（DLX: pethome.biz.exchange / order.timeout）
 *        │
 *        ▼
 *   pethome.order.timeout.queue ──► 消费端（手动 ACK）
 *        │ 处理失败 basicNack(requeue=false)
 *        ▼
 *   pethome.dlx.exchange ──► pethome.order.timeout.dlq ──► 补偿监听器（重试 <3 次重新入队，否则告警落日志）
 */
@Configuration
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class RabbitMqConfig {

    /** 延迟交换机（消息先进入 TTL 队列） */
    public static final String DELAY_EXCHANGE = "pethome.delay.exchange";
    public static final String DELAY_ORDER_QUEUE = "pethome.delay.order.queue";

    /** 业务交换机（投递到真正被消费的队列） */
    public static final String BIZ_EXCHANGE = "pethome.biz.exchange";
    public static final String ORDER_TIMEOUT_QUEUE = "pethome.order.timeout.queue";

    /** 死信交换机与队列 */
    public static final String DLX_EXCHANGE = "pethome.dlx.exchange";
    public static final String ORDER_TIMEOUT_DLQ = "pethome.order.timeout.dlq";

    public static final String RK_ORDER_TIMEOUT = "order.timeout";
    public static final String RK_ORDER_TIMEOUT_DELAY = "order.timeout.delay";
    public static final String RK_ORDER_TIMEOUT_DLQ = "order.timeout.dlq";

    /** 订单支付超时时间（分钟），与 OrderAutoCancelTask 保持一致 */
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    /** 重试次数消息头（死信补偿时自增） */
    public static final String HEADER_RETRY_COUNT = "x-retry-count";

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE, true, false);
    }

    /** TTL 队列：队列级 TTL 对固定时长的订单超时场景最合适，到期自动转死信 */
    @Bean
    public Queue delayOrderQueue() {
        return QueueBuilder.durable(DELAY_ORDER_QUEUE)
                .ttl(ORDER_TIMEOUT_MINUTES * 60 * 1000)
                .deadLetterExchange(BIZ_EXCHANGE)
                .deadLetterRoutingKey(RK_ORDER_TIMEOUT)
                .build();
    }

    @Bean
    public Binding delayOrderBinding() {
        return BindingBuilder.bind(delayOrderQueue()).to(delayExchange()).with(RK_ORDER_TIMEOUT_DELAY);
    }

    @Bean
    public DirectExchange bizExchange() {
        return new DirectExchange(BIZ_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(RK_ORDER_TIMEOUT_DLQ)
                .build();
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue()).to(bizExchange()).with(RK_ORDER_TIMEOUT);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutDlq() {
        return QueueBuilder.durable(ORDER_TIMEOUT_DLQ).build();
    }

    @Bean
    public Binding orderTimeoutDlqBinding() {
        return BindingBuilder.bind(orderTimeoutDlq()).to(dlxExchange()).with(RK_ORDER_TIMEOUT_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 手动 ACK + 单消费者 + prefetch=1。
     * 单消费者保证同一队列内消息严格有序（订单 a 的超时取消先于订单 b），
     * 手动 ACK 让“业务处理成功”才确认，进程崩溃时消息自动回队。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
