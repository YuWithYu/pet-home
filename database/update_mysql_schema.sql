-- 更新MySQL数据库表结构
USE pet_home;

-- 检查并添加appointment表的核销码字段
-- 注意：MySQL 8.0以下版本不支持IF NOT EXISTS，需要手动检查
ALTER TABLE appointment 
ADD COLUMN verify_code VARCHAR(100) COMMENT '核销码';

ALTER TABLE appointment 
ADD COLUMN is_verified INT DEFAULT 0 COMMENT '是否已核销 0-未核销 1-已核销';

ALTER TABLE appointment 
ADD COLUMN verify_time TIMESTAMP NULL COMMENT '核销时间';

-- 创建核销码索引
CREATE INDEX idx_appointment_verify_code ON appointment(verify_code);

-- 检查pets表是否存在，如果不存在则创建
CREATE TABLE pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '宠物名称',
    species VARCHAR(50) COMMENT '物种',
    breed VARCHAR(50) COMMENT '品种',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别',
    avatar VARCHAR(255) COMMENT '头像',
    description TEXT COMMENT '描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物表';

-- 创建宠物表索引
CREATE INDEX idx_pets_user_id ON pets(user_id);
CREATE INDEX idx_pets_status ON pets(status);

-- 插入一些测试宠物数据（如果表为空）
INSERT IGNORE INTO pets (user_id, name, species, breed, age, gender, avatar, description, status) VALUES
(5, '小金', 'dog', 'golden', 2, 'male', '/static/default-pet.png', '一只可爱的金毛', 'active'),
(5, '小白', 'cat', 'shorthair', 1, 'female', '/static/default-pet.png', '一只温顺的短毛猫', 'active');
