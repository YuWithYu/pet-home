-- 预约信息表
CREATE TABLE IF NOT EXISTS `bookings` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
  `service_type` VARCHAR(50) NOT NULL COMMENT '服务类型: litter/boarding/medical/grooming/adoption',
  `service_id` BIGINT COMMENT '服务ID',
  `service_name` VARCHAR(200) NOT NULL COMMENT '服务名称',
  `user_id` BIGINT COMMENT '用户ID',
  `booking_date` DATE NOT NULL COMMENT '预约日期',
  `time_slot` VARCHAR(50) NOT NULL COMMENT '预约时间段',
  `contact_name` VARCHAR(100) NOT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `address` VARCHAR(500) COMMENT '服务地址',
  `pet_info` TEXT COMMENT '宠物信息',
  `remark` TEXT COMMENT '备注',
  `total_price` DECIMAL(10, 2) NOT NULL COMMENT '总价',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending待确认/confirmed已确认/completed已完成/cancelled已取消',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_service_type` (`service_type`),
  INDEX `idx_booking_date` (`booking_date`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约信息表';

-- 时间段配置表
CREATE TABLE IF NOT EXISTS `time_slots` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '时间段ID',
  `service_type` VARCHAR(50) NOT NULL COMMENT '服务类型: litter/boarding/medical/grooming/adoption',
  `time_slot` VARCHAR(50) NOT NULL COMMENT '时间段，如: 09:00-10:00',
  `max_bookings` INT DEFAULT 10 COMMENT '最大预约数',
  `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_service_type` (`service_type`),
  UNIQUE KEY `uk_service_time` (`service_type`, `time_slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段配置表';

-- 插入默认时间段
INSERT INTO `time_slots` (`service_type`, `time_slot`, `max_bookings`, `is_active`) VALUES
('litter', '09:00-10:00', 10, TRUE),
('litter', '10:00-11:00', 10, TRUE),
('litter', '11:00-12:00', 10, TRUE),
('litter', '14:00-15:00', 10, TRUE),
('litter', '15:00-16:00', 10, TRUE),
('litter', '16:00-17:00', 10, TRUE),
('litter', '17:00-18:00', 10, TRUE),
('boarding', '09:00-10:00', 5, TRUE),
('boarding', '10:00-11:00', 5, TRUE),
('boarding', '14:00-15:00', 5, TRUE),
('boarding', '15:00-16:00', 5, TRUE),
('medical', '09:00-10:00', 8, TRUE),
('medical', '10:00-11:00', 8, TRUE),
('medical', '14:00-15:00', 8, TRUE),
('medical', '15:00-16:00', 8, TRUE),
('medical', '16:00-17:00', 8, TRUE),
('grooming', '09:00-10:00', 6, TRUE),
('grooming', '10:00-11:00', 6, TRUE),
('grooming', '14:00-15:00', 6, TRUE),
('grooming', '15:00-16:00', 6, TRUE),
('adoption', '09:00-12:00', 20, TRUE),
('adoption', '14:00-17:00', 20, TRUE)
ON DUPLICATE KEY UPDATE `max_bookings` = VALUES(`max_bookings`), `is_active` = VALUES(`is_active`);

