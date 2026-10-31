-- ============================================================
-- 2026-10-30 消息中间件基础设施
-- 1. mq_outbox：本地消息表（Transactional Outbox），保证“业务落库”与“消息投递”最终一致
-- 2. mq_consume_log：消费幂等表，唯一键拦截重复投递
-- ============================================================

CREATE TABLE IF NOT EXISTS `mq_outbox` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_key`         VARCHAR(128) NOT NULL COMMENT '业务唯一键（幂等/防重）',
    `exchange_name`   VARCHAR(100) NOT NULL COMMENT '目标交换机',
    `routing_key`     VARCHAR(100) NOT NULL COMMENT '路由键',
    `payload`         TEXT         NOT NULL COMMENT '消息体（JSON）',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待投递 1已投递 2已失败',
    `retry_count`     INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time` DATETIME     NOT NULL COMMENT '下次可投递时间',
    `last_error`      VARCHAR(500) DEFAULT NULL COMMENT '最近一次失败原因',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outbox_biz_key` (`biz_key`),
    KEY `idx_outbox_status_retry` (`status`, `next_retry_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '本地消息表（Outbox）';

CREATE TABLE IF NOT EXISTS `mq_consume_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `queue_name`   VARCHAR(100) NOT NULL COMMENT '队列名',
    `biz_key`      VARCHAR(128) NOT NULL COMMENT '业务唯一键',
    `consume_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_consume_queue_biz` (`queue_name`, `biz_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息消费幂等记录';
