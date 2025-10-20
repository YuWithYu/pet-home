-- Create grooming_appointments table for pet grooming appointment orders
DROP TABLE IF EXISTS `grooming_appointments`;

CREATE TABLE `grooming_appointments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `pet_id` bigint NOT NULL COMMENT 'Pet ID',
  `service_id` bigint DEFAULT NULL COMMENT 'Grooming service ID',
  `service_type` varchar(50) DEFAULT 'grooming' COMMENT 'Service type',
  `date` date NOT NULL COMMENT 'Appointment date',
  `time_slot` varchar(20) NOT NULL COMMENT 'Appointment time slot',
  `location` varchar(255) DEFAULT NULL COMMENT 'Service address',
  `contact_name` varchar(50) NOT NULL COMMENT 'Contact name',
  `contact_phone` varchar(20) NOT NULL COMMENT 'Contact phone',
  `remark` text COMMENT 'Remark',
  `price` decimal(10,2) NOT NULL COMMENT 'Service price',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'Status: pending, confirmed, cancelled, completed',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_date` (`date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Pet grooming appointment orders';

-- Insert test data
INSERT INTO `grooming_appointments` (`user_id`, `pet_id`, `service_id`, `service_type`, `date`, `time_slot`, `location`, `contact_name`, `contact_phone`, `remark`, `price`, `status`) VALUES
(17, 1, 26, 'grooming', '2025-10-25', '10:00-11:00', 'Test address 123', 'Zhang San', '13800138000', 'Test grooming appointment', 99.00, 'pending'),
(17, 1, 26, 'grooming', '2025-10-26', '14:00-15:00', 'Test address 456', 'Li Si', '13900139000', 'Second grooming appointment', 120.00, 'confirmed');

