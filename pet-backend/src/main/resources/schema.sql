-- H2 数据库初始化脚本
-- 注意：将 user 表改为 sys_user 以避免 H2 保留关键字冲突

-- 创建用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    avatar VARCHAR(255),
    nickname VARCHAR(50),
    gender INTEGER DEFAULT 0,
    birthday DATE,
    address VARCHAR(255),
    status INTEGER DEFAULT 1,
    role VARCHAR(20) DEFAULT 'user',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建轮播图表
DROP TABLE IF EXISTS banner;
CREATE TABLE banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    description VARCHAR(500),
    filename VARCHAR(255),
    original_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建商品表
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER DEFAULT 0,
    category VARCHAR(50),
    brand VARCHAR(50),
    image VARCHAR(255),
    status VARCHAR(20) DEFAULT 'onsale',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建宠物表
DROP TABLE IF EXISTS pet;
CREATE TABLE pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(50),
    breed VARCHAR(50),
    gender VARCHAR(10),
    birthday DATE,
    age INTEGER,
    weight DECIMAL(5, 2),
    color VARCHAR(50),
    description TEXT,
    avatar VARCHAR(255),
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建帖子表
DROP TABLE IF EXISTS post;
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    category VARCHAR(50),
    images VARCHAR(1000),
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建订单表
DROP TABLE IF EXISTS pet_order;
CREATE TABLE pet_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(10, 2) NOT NULL,
    status INTEGER DEFAULT 0,
    payment_status INTEGER DEFAULT 0,
    delivery_status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建购物车表
DROP TABLE IF EXISTS shopping_cart;
CREATE TABLE shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

-- 创建分类表
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INTEGER DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    icon VARCHAR(255),
    status INTEGER DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建宠物寄养表
DROP TABLE IF EXISTS pet_boarding;
CREATE TABLE pet_boarding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    pet_id INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    service_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    remark TEXT,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    price DECIMAL(10, 2),
    location VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建宠物领养表
DROP TABLE IF EXISTS pet_adoption;
CREATE TABLE pet_adoption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_name VARCHAR(100) NOT NULL,
    breed VARCHAR(50),
    age INTEGER,
    gender VARCHAR(10),
    description TEXT,
    image_url VARCHAR(500),
    adoption_fee DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'available',
    location VARCHAR(255),
    contact_info VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建铲屎服务表
DROP TABLE IF EXISTS litter_services;
CREATE TABLE litter_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    duration INTEGER,
    image_url VARCHAR(500),
    bg_color VARCHAR(20),
    tags JSON,
    status VARCHAR(20) DEFAULT 'active',
    is_recommended BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 创建铲屎服务展示图表
DROP TABLE IF EXISTS litter_service_banners;
CREATE TABLE litter_service_banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    title VARCHAR(200),
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 创建宠物表
DROP TABLE IF EXISTS pet;
CREATE TABLE pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(50),
    breed VARCHAR(50),
    age INTEGER,
    gender VARCHAR(10),
    avatar VARCHAR(255),
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建预约表
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    pet_id INTEGER,
    service_type VARCHAR(50),
    appointment_date TIMESTAMP,
    date DATE,
    time_slot VARCHAR(20),
    status VARCHAR(20) DEFAULT 'pending',
    remark TEXT,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    price DECIMAL(10, 2) DEFAULT 199.00,
    location VARCHAR(100) DEFAULT '宠物医院',
    verify_code VARCHAR(100),
    is_verified INTEGER DEFAULT 0,
    verify_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_appointment_verify_code ON appointment(verify_code);
CREATE INDEX idx_appointment_user_id ON appointment(user_id);

-- 不插入任何测试数据，保持数据库为空
