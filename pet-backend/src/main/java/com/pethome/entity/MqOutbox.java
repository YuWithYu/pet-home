package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本地消息表（Transactional Outbox）。
 * 业务数据与消息记录在同一事务内落库，再由补偿任务投递，避免“业务提交成功但消息丢失”。
 */
@Data
@TableName("mq_outbox")
public class MqOutbox {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务唯一键，重复入队直接命中唯一索引 */
    private String bizKey;

    private String exchangeName;

    private String routingKey;

    /** 消息体 JSON */
    private String payload;

    /** 0 待投递、1 已投递、2 已失败（超过最大重试次数） */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;

    private String lastError;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
