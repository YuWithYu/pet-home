-- 创建宠物医院服务表
CREATE TABLE IF NOT EXISTS hospital_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Service ID',
    name VARCHAR(100) NOT NULL COMMENT 'Service Name',
    description TEXT COMMENT 'Service Description',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Service Price',
    image_url VARCHAR(255) COMMENT 'Service Image URL',
    status INT DEFAULT 1 COMMENT 'Status: 1-Enabled, 0-Disabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
);

-- 插入默认数据
INSERT INTO hospital_services (name, description, price, image_url, status) VALUES
('Pet Hospital Service', 'Professional pet medical team, advanced medical equipment, 24-hour emergency service, pet health examination', 150.00, '/static/images/hospital-banner.jpg', 1),
('Pet Health Check', 'Comprehensive health examination, including blood test, X-ray, ultrasound examination', 200.00, '/static/images/health-check.jpg', 1),
('Pet Vaccination', 'Provide various pet vaccination services to ensure pet health', 80.00, '/static/images/vaccination.jpg', 1),
('Pet Surgery Service', 'Professional pet surgery, including sterilization, tumor removal', 500.00, '/static/images/surgery.jpg', 1),
('Dog Skin Cleaning Service', 'Professional dog skin cleaning and care service', 120.00, '/static/images/dog-cleaning.jpg', 1),
('Cat Skin Cleaning Service', 'Professional cat skin cleaning and care service', 100.00, '/static/images/cat-cleaning.jpg', 1),
('Dog Bathing', 'Professional dog bathing service with premium products', 80.00, '/static/images/dog-bath.jpg', 1),
('Cat Bathing', 'Professional cat bathing service with gentle care', 60.00, '/static/images/cat-bath.jpg', 1),
('Beauty SPA', 'Luxury pet beauty and spa service', 200.00, '/static/images/beauty-spa.jpg', 1),
('Non-anesthetic Teeth Cleaning', 'Safe teeth cleaning without anesthesia', 150.00, '/static/images/teeth-cleaning.jpg', 1);
