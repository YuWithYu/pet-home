package com.pethome.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 行为埋点发布器。
 *
 * Kafka 未启用（默认）时不产生任何副作用，业务链路不受影响；
 * 启用后浏览事件异步写入 Kafka，由消费者做小时级聚合统计。
 */
@Service
public class BehaviorEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BehaviorEventPublisher.class);

    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.post-view:pethome.behavior.post-view}")
    private String postViewTopic;

    public BehaviorEventPublisher(ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
                                  ObjectMapper objectMapper) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.objectMapper = objectMapper;
    }

    /** 帖子浏览事件：以 postId 作为消息 key，保证同一帖子的行为落到同一分区（有序） */
    public void publishPostView(Long postId, Long userId) {
        KafkaTemplate<String, String> template = kafkaTemplateProvider.getIfAvailable();
        if (template == null || postId == null) {
            return;
        }
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("postId", postId);
            event.put("userId", userId);
            event.put("event", "post_view");
            event.put("ts", System.currentTimeMillis());
            template.send(postViewTopic, String.valueOf(postId), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            // 埋点属于旁路链路，任何异常都不能影响主业务
            log.warn("浏览埋点发送失败: postId={}, err={}", postId, e.getMessage());
        }
    }
}
