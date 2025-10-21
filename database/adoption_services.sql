-- 创建宠物领养服务表
CREATE TABLE `adoption_services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` text COMMENT '服务描述',
  `category` varchar(50) DEFAULT 'basic' COMMENT '服务分类：basic-基础领养服务，love-爱心领养服务，professional-专业领养服务',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '服务价格',
  `duration` int DEFAULT '60' COMMENT '服务时长(分钟)',
  `image_url` varchar(255) DEFAULT NULL COMMENT '服务图片URL',
  `bg_color` varchar(20) DEFAULT '#fff3e0' COMMENT '背景颜色',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `is_recommended` tinyint(1) DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `tags` text COMMENT '服务标签，JSON格式',
  `status` varchar(20) DEFAULT 'active' COMMENT '服务状态：active-启用，inactive-禁用',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 插入示例数据
INSERT INTO `adoption_services` (`name`, `description`, `category`, `price`, `duration`, `image_url`, `bg_color`, `sort_order`, `is_recommended`, `tags`, `status`) VALUES
('基础领养服务', '提供基础的宠物领养咨询和指导服务', 'basic', 0.00, 60, '/static/images/adoption-basic.jpg', '#fff3e0', 1, 1, '["领养咨询", "基础指导"]', 'active'),
('爱心领养服务', '提供爱心宠物领养服务，包含健康检查和疫苗接种', 'love', 200.00, 120, '/static/images/adoption-love.jpg', '#f8e8e8', 2, 1, '["爱心服务", "健康检查", "疫苗接种"]', 'active'),
('专业领养服务', '提供专业宠物领养服务，包含完整健康检查和专业指导', 'professional', 500.00, 180, '/static/images/adoption-professional.jpg', '#e8f4f8', 3, 0, '["专业服务", "完整检查", "专业指导"]', 'active');
