-- Create boarding_appointments table for pet boarding appointment orders
DROP TABLE IF EXISTS `boarding_appointments`;

CREATE TABLE `boarding_appointments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `pet_id` bigint NOT NULL COMMENT 'Pet ID',
  `service_id` bigint DEFAULT NULL COMMENT 'Boarding service ID',
  `service_type` varchar(50) DEFAULT 'boarding' COMMENT 'Service type',
  `start_date` date NOT NULL COMMENT 'Boarding start date',
  `end_date` date NOT NULL COMMENT 'Boarding end date',
  `days` int NOT NULL DEFAULT 1 COMMENT 'Boarding days',
  `time_slot` varchar(20) DEFAULT NULL COMMENT 'Drop-off time slot',
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
  KEY `idx_start_date` (`start_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Pet boarding appointment orders';

-- Insert test data
INSERT INTO `boarding_appointments` (`user_id`, `pet_id`, `service_id`, `service_type`, `start_date`, `end_date`, `days`, `time_slot`, `location`, `contact_name`, `contact_phone`, `remark`, `price`, `status`) VALUES
(17, 1, 1, 'boarding', '2025-10-25', '2025-10-27', 2, '09:00-10:00', 'Pet Boarding Center 123 Main St', 'Zhang San', '13800138000', 'Test boarding appointment', 200.00, 'pending'),
(17, 1, 1, 'boarding', '2025-11-01', '2025-11-05', 4, '14:00-15:00', 'Pet Boarding Center 456 Park Ave', 'Li Si', '13900139000', 'Second boarding appointment', 400.00, 'confirmed');

