-- 分离不同服务的预约表
USE pet_home;

-- 1. 创建宠物医院预约表
CREATE TABLE `hospital_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pet_id` INT COMMENT '宠物ID',
  `service_type` VARCHAR(50) DEFAULT 'hospital' COMMENT '服务类型',
  `appointment_date` DATE COMMENT '预约日期',
  `time_slot` VARCHAR(20) COMMENT '时间段',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `remark` TEXT COMMENT '备注',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `price` DECIMAL(10, 2) DEFAULT 199.00 COMMENT '价格',
  `location` VARCHAR(100) DEFAULT '宠物医院' COMMENT '预约地点',
  `verify_code` VARCHAR(100) COMMENT '核销码',
  `is_verified` INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销',
  `verify_time` TIMESTAMP NULL COMMENT '核销时间',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物医院预约表';

-- 2. 创建上门铲屎服务预约表
CREATE TABLE `door_cleaning_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pet_id` INT COMMENT '宠物ID',
  `service_type` VARCHAR(50) DEFAULT 'door-cleaning' COMMENT '服务类型',
  `appointment_date` DATE COMMENT '预约日期',
  `time_slot` VARCHAR(20) COMMENT '时间段',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `remark` TEXT COMMENT '备注',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `price` DECIMAL(10, 2) DEFAULT 199.00 COMMENT '价格',
  `location` VARCHAR(100) DEFAULT '广州南方学院店' COMMENT '预约地点',
  `verify_code` VARCHAR(100) COMMENT '核销码',
  `is_verified` INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销',
  `verify_time` TIMESTAMP NULL COMMENT '核销时间',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上门铲屎服务预约表';

-- 3. 创建宠物寄养预约表
CREATE TABLE `pet_fostering_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pet_id` INT COMMENT '宠物ID',
  `service_type` VARCHAR(50) DEFAULT 'fostering' COMMENT '服务类型',
  `appointment_date` DATE COMMENT '预约日期',
  `time_slot` VARCHAR(20) COMMENT '时间段',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `remark` TEXT COMMENT '备注',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `price` DECIMAL(10, 2) DEFAULT 299.00 COMMENT '价格',
  `location` VARCHAR(100) DEFAULT '宠物寄养中心' COMMENT '预约地点',
  `verify_code` VARCHAR(100) COMMENT '核销码',
  `is_verified` INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销',
  `verify_time` TIMESTAMP NULL COMMENT '核销时间',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物寄养预约表';

-- 4. 创建宠物领养预约表
CREATE TABLE `pet_adoption_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pet_id` INT COMMENT '宠物ID',
  `service_type` VARCHAR(50) DEFAULT 'adoption' COMMENT '服务类型',
  `appointment_date` DATE COMMENT '预约日期',
  `time_slot` VARCHAR(20) COMMENT '时间段',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `remark` TEXT COMMENT '备注',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格（领养通常免费）',
  `location` VARCHAR(100) DEFAULT '宠物领养中心' COMMENT '预约地点',
  `verify_code` VARCHAR(100) COMMENT '核销码',
  `is_verified` INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销',
  `verify_time` TIMESTAMP NULL COMMENT '核销时间',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物领养预约表';

-- 5. 创建看护服务预约表
CREATE TABLE `pet_care_appointment` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pet_id` INT COMMENT '宠物ID',
  `service_type` VARCHAR(50) DEFAULT 'care' COMMENT '服务类型',
  `appointment_date` DATE COMMENT '预约日期',
  `time_slot` VARCHAR(20) COMMENT '时间段',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成',
  `remark` TEXT COMMENT '备注',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `price` DECIMAL(10, 2) DEFAULT 149.00 COMMENT '价格',
  `location` VARCHAR(100) DEFAULT '看护服务中心' COMMENT '预约地点',
  `verify_code` VARCHAR(100) COMMENT '核销码',
  `is_verified` INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销',
  `verify_time` TIMESTAMP NULL COMMENT '核销时间',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_verify_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='看护服务预约表';

-- 6. 迁移现有数据到对应的表
-- 迁移door-cleaning服务的数据到上门铲屎服务预约表
INSERT INTO door_cleaning_appointment (
  user_id, pet_id, service_type, appointment_date, time_slot, status, remark,
  contact_name, contact_phone, price, location, verify_code, is_verified, verify_time,
  create_time, update_time
)
SELECT 
  user_id, pet_id, service_type, date, time_slot, status, remark,
  contact_name, contact_phone, price, location, verify_code, is_verified, verify_time,
  create_time, update_time
FROM appointment 
WHERE service_type = 'door-cleaning' OR service_type = 'litter';

-- 迁移其他服务的数据到宠物医院预约表（作为默认）
INSERT INTO hospital_appointment (
  user_id, pet_id, service_type, appointment_date, time_slot, status, remark,
  contact_name, contact_phone, price, location, verify_code, is_verified, verify_time,
  create_time, update_time
)
SELECT 
  user_id, pet_id, 
  CASE 
    WHEN service_type IS NULL OR service_type = '' THEN 'hospital'
    ELSE service_type
  END as service_type,
  date, time_slot, status, remark,
  contact_name, contact_phone, price, location, verify_code, is_verified, verify_time,
  create_time, update_time
FROM appointment 
WHERE service_type != 'door-cleaning' AND service_type != 'litter';

-- 7. 备份原appointment表
CREATE TABLE appointment_backup AS SELECT * FROM appointment;

-- 8. 删除原appointment表（可选，建议先保留）
-- DROP TABLE appointment;
