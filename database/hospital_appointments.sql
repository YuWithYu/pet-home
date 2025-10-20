-- Pet hospital appointments table
CREATE TABLE hospital_appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    pet_id BIGINT NOT NULL COMMENT 'Pet ID',
    service_type VARCHAR(50) DEFAULT 'hospital' COMMENT 'Service type',
    date DATE NOT NULL COMMENT 'Appointment date',
    time_slot VARCHAR(20) NOT NULL COMMENT 'Appointment time slot',
    location VARCHAR(255) COMMENT 'Service address',
    contact_name VARCHAR(50) NOT NULL COMMENT 'Contact name',
    contact_phone VARCHAR(20) NOT NULL COMMENT 'Contact phone',
    remark TEXT COMMENT 'Remark',
    price DECIMAL(10,2) NOT NULL COMMENT 'Service price',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'Status: pending-confirmed, confirmed-confirmed, cancelled-cancelled, completed-completed',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX idx_user_id (user_id),
    INDEX idx_pet_id (pet_id),
    INDEX idx_date (date),
    INDEX idx_status (status)
);

-- Insert test data
INSERT INTO hospital_appointments (user_id, pet_id, service_type, date, time_slot, location, contact_name, contact_phone, remark, price, status) VALUES
(17, 1, 'hospital', '2025-01-15', '10:00-12:00', 'Guangzhou Yuexiu Pet Hospital', 'Zhang San', '13800138000', 'Regular checkup', 150.00, 'confirmed'),
(17, 2, 'hospital', '2025-01-16', '14:00-16:00', 'Guangzhou Tianhe Pet Hospital', 'Li Si', '13900139000', 'Vaccination', 80.00, 'pending');