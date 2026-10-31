package com.pethome.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 行为埋点消费者：按小时聚合浏览事件到 Redis Hash，供运营看板读取。
 * 手动提交偏移（MANUAL_IMMEDIATE），处理成功后才 ack，失败时可由 Kafka 重新投递。
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class BehaviorEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BehaviorEventConsumer.class);

    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = "${app.kafka.topic.post-view:pethome.behavior.post-view}",
            groupId = "pethome-analytics",
            containerFactory = "kafkaListenerContainerFactory")
    public void onPostView(String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            long postId = node.path("postId").asLong(0);
            if (postId <= 0) {
                ack.acknowledge();
                return;
            }
            String hour = LocalDateTime.now().format(HOUR_FORMAT);
            String key = "post:view:hourly:" + hour;
            stringRedisTemplate.opsForHash().increment(key, String.valueOf(postId), 1L);
            stringRedisTemplate.expire(key, Duration.ofDays(7));
            ack.acknowledge();
        } catch (Exception e) {
            log.error("行为埋点消费失败，等待 Kafka 重投: payload={}", payload, e);
            // 不 ack，由容器在下次拉取时重试
        }
    }
}
