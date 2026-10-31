package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息消费幂等记录：唯一键 (queue_name, biz_key) 拦截重复投递。
 */
@Data
@TableName("mq_consume_log")
public class MqConsumeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String queueName;

    private String bizKey;

    private LocalDateTime consumeTime;
}
