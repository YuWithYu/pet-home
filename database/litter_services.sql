-- 铲屎服务表
CREATE TABLE IF NOT EXISTS `litter_services` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) NOT NULL COMMENT '服务名称',
  `description` text COMMENT '服务描述',
  `price` decimal(10,2) NOT NULL COMMENT '服务价格',
  `duration` int(11) DEFAULT 60 COMMENT '服务时长(分钟)',
  `category` varchar(100) DEFAULT 'basic' COMMENT '服务分类',
  `image_url` varchar(500) COMMENT '服务图片URL',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='铲屎服务表';

-- 插入默认铲屎服务数据
INSERT INTO `litter_services` (`name`, `description`, `price`, `duration`, `category`, `image_url`, `status`, `sort_order`, `created_at`, `updated_at`, `is_deleted`) 
VALUES 
('基础铲屎服务', '专业上门铲屎服务，包含基础清洁和消毒', 99.00, 30, 'basic', '/static/images/door-cleaning.svg', 'active', 1, NOW(), NOW(), 0),
('深度清洁服务', '深度清洁服务，包含彻底清洁、除臭和消毒', 149.00, 60, 'deep-cleaning', '/static/images/door-cleaning.svg', 'active', 2, NOW(), NOW(), 0),
('定期维护服务', '定期维护服务，适合长期合作客户', 199.00, 45, 'regular', '/static/images/door-cleaning.svg', 'active', 3, NOW(), NOW(), 0),
('紧急清理服务', '紧急清理服务，快速响应，2小时内上门', 129.00, 40, 'emergency', '/static/images/door-cleaning.svg', 'active', 4, NOW(), NOW(), 0);
