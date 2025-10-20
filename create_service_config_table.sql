-- 创建服务配置表
CREATE TABLE IF NOT EXISTS `service_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务配置ID',
  `service_type` varchar(50) NOT NULL COMMENT '服务类型标识（如：door-cleaning, grooming等）',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` text COMMENT '服务描述',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '服务价格',
  `icon` varchar(255) COMMENT '服务图标路径',
  `image` varchar(255) COMMENT '服务图片路径',
  `time_slots` text COMMENT '可预约时间段（JSON格式）',
  `service_area` varchar(255) COMMENT '服务区域',
  `max_bookings_per_day` int DEFAULT 10 COMMENT '每日最大预约数',
  `status` int DEFAULT 1 COMMENT '状态（1-启用，0-禁用）',
  `sort_order` int DEFAULT 0 COMMENT '排序顺序',
  `features` text COMMENT '服务特点（JSON格式）',
  `notice` text COMMENT '温馨提示（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_type` (`service_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务配置表';

-- 插入默认服务配置
INSERT INTO `service_config` (`service_type`, `service_name`, `description`, `price`, `icon`, `time_slots`, `features`, `notice`, `sort_order`, `status`) VALUES
('door-cleaning', '上门铲屎服务', '专业贴心，让您的宠物生活更舒适', 99.00, '/static/images/door-cleaning.svg', 
'["08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00", "18:00-20:00"]',
'["上门服务|专业人员上门为您的宠物清理猫砂狗便", "灵活时间|可预约多个时间段，满足不同需求", "清洁彻底|不仅清理排泄物，还会进行环境消毒", "安全保障|经过专业培训，持证上岗，服务有保障"]',
'["请提前预约，至少提前1天", "服务时间为早8:00-晚8:00", "首次服务需要留下门禁密码或钥匙", "如有监控建议开启，确保双方安全", "遇到特殊情况可联系客服改期"]',
1, 1),

('grooming', '宠物洗护服务', '专业美容师上门为您的宠物提供洗护服务', 150.00, '/static/images/pet-grooming.svg',
'["09:00-11:00", "11:00-13:00", "14:00-16:00", "16:00-18:00"]',
'["专业美容师|持证上岗，经验丰富", "全套设备|自带专业洗护设备和用品", "温柔细致|温柔对待每一只宠物", "环境友好|使用天然无刺激洗护用品"]',
'["请提前预约，至少提前2天", "服务时间1-2小时", "请准备充足的热水", "长毛宠物可能需要额外费用", "服务前请告知宠物特殊情况"]',
2, 1),

('boarding', '宠物寄养服务', '专业寄养场所，让您的宠物安心度过', 80.00, '/static/images/pet-boarding.svg',
'["全天"]',
'["专业照料|24小时专人照看", "独立空间|干净舒适的独立寄养空间", "定时喂食|按时按量科学喂养", "每日汇报|每天发送宠物照片和视频"]',
'["至少提前3天预约", "需提供宠物疫苗证明", "请自带宠物日常用品", "寄养期间如有异常及时联系", "价格为每天80元"]',
3, 1),

('hospital', '宠物医疗预约', '专业宠物医院，提供全面的医疗服务', 199.00, '/static/images/pet-hospital.svg',
'["09:00-12:00", "14:00-17:00", "19:00-21:00"]',
'["专业医师|资深兽医坐诊", "设备齐全|先进的医疗设备", "全面检查|提供全面的健康检查", "急诊服务|提供24小时急诊服务"]',
'["请提前预约挂号", "带好宠物病历本", "就诊前禁食2小时", "急诊随时接诊", "费用根据实际诊疗项目结算"]',
4, 1);
