SET NAMES utf8mb4;

-- Create database
CREATE DATABASE IF NOT EXISTS house_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE house_trading;

-- User Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('ADMIN', 'LANDLORD', 'USER') DEFAULT 'USER',
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '1: Active, 0: Disabled',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- House Table
CREATE TABLE IF NOT EXISTS houses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    landlord_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    area DECIMAL(10, 2) NOT NULL,
    house_type VARCHAR(50) COMMENT 'e.g., 3室2厅',
    orientation VARCHAR(20),
    floor INT,
    total_floor INT,
    status ENUM('PENDING', 'ACTIVE', 'REJECTED', 'SOLD', 'ARCHIVED') DEFAULT 'PENDING',
    rejection_reason TEXT,
    image_url VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Appointment Table
CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    house_id INT NOT NULL,
    landlord_id INT,
    appointment_time DATETIME NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Contract Table
CREATE TABLE IF NOT EXISTS contracts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    landlord_id INT NOT NULL,
    house_id INT NOT NULL,
    contract_content TEXT,
    total_amount DECIMAL(12, 2),
    status ENUM('DRAFT', 'SIGNED', 'COMPLETED') DEFAULT 'DRAFT',
    sign_time DATETIME,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Collections Table
CREATE TABLE IF NOT EXISTS collections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    house_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, house_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    house_id INT NOT NULL,
    content TEXT,
    rating TINYINT DEFAULT 5,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Announcements Table
CREATE TABLE IF NOT EXISTS announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Password Reset Token Table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expiry_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Search History Table
CREATE TABLE IF NOT EXISTS search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_create (user_id, create_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Payment Table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL,
    payer_id INT NOT NULL,
    payee_id INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'CANCELLED') DEFAULT 'PENDING',
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payee_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Seed Data
INSERT INTO users (username, password, email, phone, role, avatar) VALUES
('admin', '$2a$12$dyHGfnq9tgSMUoyg0QGJ5e2EHsZZf9zYGaLoX8eWEJKU1YLK9Kmxq', 'admin@example.com', '13800138000', 'ADMIN', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'),
('landlord', '$2a$12$dyHGfnq9tgSMUoyg0QGJ5e2EHsZZf9zYGaLoX8eWEJKU1YLK9Kmxq', 'landlord@example.com', '13800138001', 'LANDLORD', 'https://api.dicebear.com/7.x/avataaars/svg?seed=landlord'),
('user', '$2a$12$dyHGfnq9tgSMUoyg0QGJ5e2EHsZZf9zYGaLoX8eWEJKU1YLK9Kmxq', 'user@example.com', '13800138002', 'USER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user');

-- Insert some houses
INSERT INTO houses (landlord_id, title, description, address, price, area, house_type, orientation, floor, total_floor, status, image_url) VALUES
(2, '静安区精致两居室', '交通便利，精装修，采光好', '上海市静安区南京西路100号', 5500000.00, 85.50, '2室1厅', '南', 12, 20, 'ACTIVE', 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&q=80&w=400'),
(2, '徐汇区豪华三居室', '近地铁，江景房，学区房', '上海市徐汇区龙腾大道200号', 12000000.00, 145.00, '3室2厅', '西南', 25, 30, 'ACTIVE', 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&q=80&w=400'),
(2, '浦东新区 陆家嘴 滨江豪宅 观景大平层', '陆家嘴核心板块，一线江景，精湛装修，家电全齐，拎包入住。', '上海市浦东新区滨江大道88号', 28000000.00, 245.50, '4室2厅', '南北', 15, 30, 'ACTIVE', 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '长宁区 中山公园 采光极佳 两居室', '近地铁，交通便利，周边生活设施齐全，户型方正，采光无遮挡。', '上海市长宁区长宁路1000号', 7500000.00, 88.00, '2室1厅', '朝南', 8, 24, 'ACTIVE', 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '黄浦区 新天地 稀缺洋房 历史底蕴', '新天地老洋房改造，保留原始风貌，内部现代化精装，独门独栋。', '上海市黄浦区马当路300号', 35000000.00, 150.00, '3室2厅', '南北', 1, 3, 'ACTIVE', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '闵行区 莘庄 核心商圈 优质刚需小三房', '莘庄地铁站附近，高性价比，适合家庭居住，小区绿化率高。', '上海市闵行区莘建东路198号', 5800000.00, 95.50, '3室1厅', '南', 12, 18, 'ACTIVE', 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '杨浦区 五角场 创新型Loft 拎包入住', '紧邻复旦、同济大学，生活气息浓厚，适合年轻人，复式结构创意空间。', '上海市杨浦区淞沪路333号', 4200000.00, 60.00, '1室1厅', '朝东', 6, 10, 'ACTIVE', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '静安区 大宁金茂府 品质社区 科技住宅', '金茂科技系统，恒温恒湿，高端物业，居住体验极佳。', '上海市静安区彭江路333号', 16500000.00, 135.00, '3室2厅', '南北', 20, 28, 'ACTIVE', 'https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '普陀区 长风生态商务区 豪华四居', '紧邻长风公园，天然氧吧，高端学区，四开间朝南。', '上海市普陀区大渡河路555号', 18500000.00, 160.00, '4室2厅', '南北', 9, 22, 'ACTIVE', 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '虹口区 北外滩 滨江一线 江景豪宅', '北外滩核心地段，全景落地窗，无遮挡江景，奢华装修。', '上海市虹口区东大名路888号', 45000000.00, 300.00, '4室2厅', '东南', 25, 35, 'ACTIVE', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '徐汇区 徐家汇核心景观 顶层复式', '徐家汇商圈，顶层带大露台，无敌视野，尊享私密空间。', '上海市徐汇区肇嘉浜路1000号', 22000000.00, 180.00, '4室2厅', '南北', 30, 30, 'PENDING', 'https://images.unsplash.com/photo-1502672260266-1c1de24220e8?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),
(2, '浦东新区 张江高科 品质三居 程序员首选', '近张江软件园，上下班方便，精装三房，适合家庭。', '上海市浦东新区张江路100号', 8500000.00, 110.00, '3室2厅', '朝南', 14, 18, 'PENDING', 'https://images.unsplash.com/photo-1502005097973-bf520fb7ac8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80');
