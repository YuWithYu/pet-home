-- 铲屎服务展示图表
CREATE TABLE IF NOT EXISTS `litter_service_banners` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '展示图标题',
  `description` text COMMENT '展示图描述',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `position` varchar(100) NOT NULL COMMENT '展示位置',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='铲屎服务展示图表';

-- 插入默认数据
INSERT INTO `litter_service_banners` (`title`, `description`, `image_url`, `position`, `status`, `sort_order`, `created_at`, `updated_at`, `is_deleted`) 
VALUES 
('铲屎服务展示图', '铲屎服务页面顶部展示图', '/static/images/door-cleaning.svg', 'litter-page-top', 'active', 1, NOW(), NOW(), 0);
