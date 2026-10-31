-- ============================================================
-- 2026-10-30 预约并发安全：时间段占用令牌 + 数据库唯一索引兜底
-- 说明：Redis 分布式锁只做“削峰”，唯一索引是并发正确性的最终裁决。
--       取消/完成/爽约/核销时应用层将 slot_token 置 NULL（唯一索引允许多个 NULL）。
-- ============================================================

ALTER TABLE `appointment`
    ADD COLUMN `slot_token` VARCHAR(128) NULL
        COMMENT '时间段占用令牌（serviceType|date|timeSlot|memberId）' AFTER `status`;

CREATE UNIQUE INDEX `uk_apt_slot_token` ON `appointment` (`slot_token`);

-- 回填存量占用记录（仅对仍占用时段的预约生效）
-- 注意：若历史数据已存在同一时段多条占用记录，回填会触发唯一键冲突，
--       需先人工清理重复预约后再执行本段。
UPDATE `appointment`
SET `slot_token` = CONCAT(
        IFNULL(`service_type`, 'null'), '|',
        IFNULL(DATE_FORMAT(`date`, '%Y-%m-%d'), 'null'), '|',
        IFNULL(TRIM(`time_slot`), 'null'), '|',
        IFNULL(`member_id`, 'any')
)
WHERE `slot_token` IS NULL
  AND `status` NOT IN ('completed', 'cancelled', 'no_show');
