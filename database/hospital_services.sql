-- 宠物医院服务表
CREATE TABLE hospital_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '服务名称',
    description TEXT COMMENT '服务描述',
    price DECIMAL(10,2) NOT NULL COMMENT '服务价格',
    image_url VARCHAR(255) COMMENT '服务图片URL',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- Insert default data
INSERT INTO hospital_services (name, description, price, image_url, status) VALUES
('Pet Hospital Service', 'Professional pet medical team, advanced medical equipment, 24-hour emergency service, pet health examination', 150.00, '/static/images/hospital-banner.jpg', 1),
('Pet Health Check', 'Comprehensive health examination, including blood test, X-ray, ultrasound examination', 200.00, '/static/images/health-check.jpg', 1),
('Pet Vaccination', 'Provide various pet vaccination services to ensure pet health', 80.00, '/static/images/vaccination.jpg', 1),
('Pet Surgery Service', 'Professional pet surgery, including sterilization, tumor removal', 500.00, '/static/images/surgery.jpg', 1);
