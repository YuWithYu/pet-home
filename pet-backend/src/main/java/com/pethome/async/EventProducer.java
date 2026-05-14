package com.pethome.async;

import com.alibaba.fastjson.JSONObject;
import com.pethome.util.JSONUtil;
import com.pethome.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author linyuhong
 * @date 2019/9/6
 */
@Service
public class EventProducer {

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean fireEvent(EventModel eventModel) {
        try {
            String eventQueueKey = RedisKeyUtil.getEventqueueKey();
            redisTemplate.opsForList().leftPush(eventQueueKey, JSONObject.toJSONString(eventModel));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
