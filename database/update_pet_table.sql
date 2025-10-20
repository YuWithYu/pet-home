-- 更新宠物表，添加缺失的字段
-- 用于支持完整的宠物资料编辑功能

-- 添加绝育状态字段
ALTER TABLE `pet` ADD COLUMN `sterilization` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'unknown' COMMENT '绝育状态: no-未绝育, yes-已绝育, unknown-不清楚';

-- 添加相亲意愿字段  
ALTER TABLE `pet` ADD COLUMN `dating` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'no' COMMENT '相亲意愿: no-没有, yes-有';

-- 添加到家日期字段
ALTER TABLE `pet` ADD COLUMN `arrival_date` date NULL DEFAULT NULL COMMENT '到家日期';

-- 添加宠物类型字段（如果不存在的话）
ALTER TABLE `pet` ADD COLUMN `pet_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'cat' COMMENT '宠物类型: cat-猫, dog-狗, other-其他';

-- 添加陪伴天数计算字段（可选，也可以在前端计算）
ALTER TABLE `pet` ADD COLUMN `companionship_days` int NULL DEFAULT 0 COMMENT '陪伴天数';

-- 添加累计获食字段
ALTER TABLE `pet` ADD COLUMN `total_food` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计获食(克)';

-- 更新现有数据的默认值
UPDATE `pet` SET 
    `sterilization` = 'unknown',
    `dating` = 'no', 
    `pet_type` = CASE 
        WHEN `species` = 'cat' THEN 'cat'
        WHEN `species` = 'dog' THEN 'dog' 
        ELSE 'other'
    END,
    `companionship_days` = CASE 
        WHEN `birthday` IS NOT NULL THEN DATEDIFF(CURDATE(), `birthday`)
        ELSE 0
    END
WHERE `sterilization` IS NULL OR `dating` IS NULL OR `pet_type` IS NULL;
