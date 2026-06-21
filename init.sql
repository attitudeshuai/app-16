CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    real_name_verified BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS driver_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    application_no VARCHAR(255) NOT NULL UNIQUE,
    id_card_front VARCHAR(255) NOT NULL,
    id_card_back VARCHAR(255) NOT NULL,
    driving_license_front VARCHAR(255) NOT NULL,
    driving_license_back VARCHAR(255) NOT NULL,
    real_name VARCHAR(255) NOT NULL,
    id_card_number VARCHAR(255) NOT NULL,
    driving_license_number VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_remark VARCHAR(500),
    reviewed_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_driver_id (driver_id),
    INDEX idx_status (status),
    INDEX idx_application_no (application_no)
);

CREATE TABLE IF NOT EXISTS driver_verification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    verification_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    old_status VARCHAR(255),
    new_status VARCHAR(255) NOT NULL,
    remark VARCHAR(500) NOT NULL,
    operation_ip VARCHAR(255),
    created_at DATETIME(6),
    INDEX idx_verification_id (verification_id),
    FOREIGN KEY (verification_id) REFERENCES driver_verifications(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    start_location VARCHAR(255) NOT NULL,
    end_location VARCHAR(255) NOT NULL,
    start_time VARCHAR(255),
    return_time VARCHAR(255),
    days_of_week VARCHAR(255),
    seats INTEGER NOT NULL,
    price_per_seat DECIMAL(19,2),
    is_active BIT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS carpools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    trip_date DATE NOT NULL,
    available_seats INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'OPEN',
    note VARCHAR(255),
    created_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS carpool_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carpool_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    seats_booked INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS carpool_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carpool_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(255),
    created_at DATETIME(6)
);

INSERT IGNORE INTO users (id, username, email, password_hash, avatar, role, real_name_verified, created_at, updated_at) VALUES
(1, 'zhangsan', 'zhangsan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 1, NOW(), NOW()),
(2, 'lisi', 'lisi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 1, NOW(), NOW()),
(3, 'wangwu', 'wangwu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 1, NOW(), NOW()),
(4, 'zhaoliu', 'zhaoliu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 1, NOW(), NOW()),
(5, 'sunqi', 'sunqi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 0, NOW(), NOW()),
(6, 'zhouba', 'zhouba@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 0, NOW(), NOW()),
(7, 'wujiu', 'wujiu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 0, NOW(), NOW()),
(8, 'zhengshi', 'zhengshi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'USER', 0, NOW(), NOW()),
(9, 'admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'ADMIN', 1, NOW(), NOW());

INSERT IGNORE INTO driver_verifications (id, driver_id, application_no, id_card_front, id_card_back, driving_license_front, driving_license_back, real_name, id_card_number, driving_license_number, status, reviewer_id, review_remark, reviewed_at, created_at, updated_at) VALUES
(1, 5, 'VR20240601120000ABC12345', '/uploads/id5_front.jpg', '/uploads/id5_back.jpg', '/uploads/license5_front.jpg', '/uploads/license5_back.jpg', '孙七', '110101199001011234', '1100001234567890', 'PENDING', NULL, NULL, NULL, NOW(), NOW()),
(2, 6, 'VR20240601130000DEF67890', '/uploads/id6_front.jpg', '/uploads/id6_back.jpg', '/uploads/license6_front.jpg', '/uploads/license6_back.jpg', '周八', '310101199203045678', '3100009876543210', 'APPROVED', 9, '证件信息真实有效，审核通过', NOW(), NOW(), NOW());

INSERT IGNORE INTO routes (id, owner_id, start_location, end_location, start_time, return_time, days_of_week, seats, price_per_seat, is_active, created_at, updated_at) VALUES
(1, 1, '望京SOHO', '中关村软件园', '08:00', '18:00', 'MON,TUE,WED,THU,FRI', 4, 15.00, 1, NOW(), NOW()),
(2, 2, '天通苑', '望京SOHO', '07:30', '18:30', 'MON,TUE,WED,THU,FRI', 3, 10.00, 1, NOW(), NOW()),
(3, 3, '回龙观', '西二旗', '08:30', '19:00', 'MON,WED,FRI', 2, 12.00, 1, NOW(), NOW()),
(4, 1, '望京SOHO', '国贸CBD', '09:00', '18:00', 'TUE,THU', 3, 20.00, 1, NOW(), NOW()),
(5, 4, '朝阳大悦城', '亦庄经济开发区', '07:00', '17:30', 'MON,TUE,WED,THU,FRI', 5, 25.00, 1, NOW(), NOW());

INSERT IGNORE INTO carpools (id, route_id, driver_id, trip_date, available_seats, status, note, created_at) VALUES
(1, 1, 1, CURDATE(), 4, 'OPEN', '早上从望京出发，准时发车', NOW()),
(2, 2, 2, CURDATE(), 3, 'OPEN', '天通苑到望京，每天固定路线', NOW()),
(3, 3, 3, CURDATE(), 2, 'FULL', '回龙观到西二旗，已有乘客', NOW()),
(4, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, 'OPEN', '明天望京到国贸', NOW()),
(5, 5, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 5, 'OPEN', '朝阳到亦庄，周末也有', NOW());

INSERT IGNORE INTO carpool_bookings (id, carpool_id, passenger_id, seats_booked, status, created_at) VALUES
(1, 1, 2, 1, 'CONFIRMED', NOW()),
(2, 1, 3, 1, 'PENDING', NOW()),
(3, 2, 5, 1, 'CONFIRMED', NOW()),
(4, 2, 6, 1, 'PENDING', NOW()),
(5, 3, 7, 1, 'CONFIRMED', NOW()),
(6, 3, 8, 1, 'CONFIRMED', NOW());

INSERT IGNORE INTO carpool_ratings (id, carpool_id, reviewer_id, reviewee_id, rating, comment, created_at) VALUES
(1, 1, 2, 1, 5, '司机准时，车况很好', NOW()),
(2, 2, 5, 2, 4, '路线方便，价格合理', NOW()),
(3, 1, 1, 2, 5, '乘客礼貌，按时到达上车点', NOW()),
(4, 2, 2, 5, 4, '准时上车，费用AA很方便', NOW());
