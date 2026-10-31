package com.pethome.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.MqConsumeLog;
import com.pethome.mapper.MqConsumeLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消费幂等：以 (queue_name, biz_key) 唯一索引作为幂等水位。
 */
@Service
@ConditionalOnProperty(name = "app.mq.enabled", havingValue = "true")
public class MqConsumeLogService {

    private static final Logger log = LoggerFactory.getLogger(MqConsumeLogService.class);

    @Autowired
    private MqConsumeLogMapper mapper;

    public boolean isConsumed(String queue, String bizKey) {
        QueryWrapper<MqConsumeLog> qw = new QueryWrapper<>();
        qw.eq("queue_name", queue).eq("biz_key", bizKey);
        return mapper.selectCount(qw) > 0;
    }

    /**
     * 记录已消费。并发重复投递时由唯一索引拦下，视为已幂等处理。
     */
    public void markConsumed(String queue, String bizKey) {
        try {
            MqConsumeLog record = new MqConsumeLog();
            record.setQueueName(queue);
            record.setBizKey(bizKey);
            record.setConsumeTime(LocalDateTime.now());
            mapper.insert(record);
        } catch (DuplicateKeyException e) {
            log.debug("消费记录已存在（幂等命中）: queue={}, bizKey={}", queue, bizKey);
        }
    }
}
