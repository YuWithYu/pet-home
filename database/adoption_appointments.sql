-- Create adoption_appointments table for pet adoption appointment orders
DROP TABLE IF EXISTS `adoption_appointments`;

CREATE TABLE `adoption_appointments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID (adopter)',
  `adoption_pet_id` bigint NOT NULL COMMENT 'Pet adoption ID (from pet_adoption table)',
  `service_type` varchar(50) DEFAULT 'adoption' COMMENT 'Service type',
  `appointment_date` date NOT NULL COMMENT 'Appointment date for visiting/pickup',
  `time_slot` varchar(20) NOT NULL COMMENT 'Appointment time slot',
  `contact_name` varchar(50) NOT NULL COMMENT 'Contact name',
  `contact_phone` varchar(20) NOT NULL COMMENT 'Contact phone',
  `id_card` varchar(18) DEFAULT NULL COMMENT 'ID card number (for adoption verification)',
  `address` varchar(255) DEFAULT NULL COMMENT 'Adopter address',
  `reason` text COMMENT 'Adoption reason/experience with pets',
  `remark` text COMMENT 'Additional remarks',
  `adoption_fee` decimal(10,2) DEFAULT 0.00 COMMENT 'Adoption fee',
  `status` varchar(20) DEFAULT 'pending' COMMENT 'Status: pending, approved, rejected, completed, cancelled',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_adoption_pet_id` (`adoption_pet_id`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Pet adoption appointment orders';

-- Insert test data
INSERT INTO `adoption_appointments` (`user_id`, `adoption_pet_id`, `service_type`, `appointment_date`, `time_slot`, `contact_name`, `contact_phone`, `id_card`, `address`, `reason`, `remark`, `adoption_fee`, `status`) VALUES
(17, 1, 'adoption', '2025-10-25', '10:00-11:00', 'Zhang San', '13800138000', '440106199001011234', 'Test Address 123', 'I love pets and have experience caring for dogs', 'Looking forward to adoption', 200.00, 'pending'),
(17, 2, 'adoption', '2025-10-26', '14:00-15:00', 'Li Si', '13900139000', '440106199101011234', 'Test Address 456', 'Have a spacious home suitable for cats', 'Can provide good living conditions', 150.00, 'approved');

