package com.pethome.async;

import com.alibaba.fastjson.JSONObject;
import com.pethome.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ConditionalOnProperty(name = "events.enabled", havingValue = "true", matchIfMissing = false)
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private ApplicationContext applicationContext;
    private Map<EventType, List<EventHandler>> config = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> supportEventTypes = entry.getValue().getSupportEventTypes();
                for (EventType eventType : supportEventTypes) {
                    config.computeIfAbsent(eventType, k -> new ArrayList<>()).add(entry.getValue());
                }
            }
        }
        Thread thread = new Thread(() -> {
            while (true) {
                String eventqueueKey = RedisKeyUtil.getEventqueueKey();
                List<Object> lists = redisTemplate.executePipelined(new RedisCallback<Object>() {
                    @Nullable
                    @Override
                    public Object doInRedis(RedisConnection connection) throws DataAccessException {
                        return connection.bRPop(0, eventqueueKey.getBytes());
                    }
                }, new StringRedisSerializer());
                for (Object obj : lists) {
                    if (obj == null) continue;
                    List<String> events = (List) obj;
                    for (String message : events) {
                        if (message.equals(eventqueueKey)) continue;
                        message = message.replace("\\", "").replace("\"{", "{").replace("}\"", "}");
                        EventModel eventModel = JSONObject.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getEventType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }
                        for (EventHandler eventHandler : config.get(eventModel.getEventType())) {
                            eventHandler.doHandler(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
