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
    average_distance_km DECIMAL(10,2),
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
    suggested_price_per_seat DECIMAL(10,2),
    final_price_per_seat DECIMAL(10,2),
    created_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS carpool_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carpool_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    seats_booked INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    emergency_contact_name VARCHAR(50),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(20),
    reminder_sms_sent BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    passenger_id BIGINT NOT NULL,
    contact_name VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(20) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_passenger_id (passenger_id)
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

CREATE TABLE IF NOT EXISTS pricing_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_price_per_km DECIMAL(10,2) NOT NULL DEFAULT 2.50,
    peak_hour_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.30,
    off_peak_multiplier DECIMAL(5,2) NOT NULL DEFAULT 0.90,
    heat_multiplier_base DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    heat_multiplier_per_booking DECIMAL(5,4) NOT NULL DEFAULT 0.0200,
    heat_multiplier_max DECIMAL(5,2) NOT NULL DEFAULT 1.50,
    seat_scarcity_threshold INTEGER NOT NULL DEFAULT 1,
    seat_scarcity_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.15,
    distance_weight DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    min_price_per_seat DECIMAL(10,2) NOT NULL DEFAULT 5.00,
    max_price_per_seat DECIMAL(10,2) NOT NULL DEFAULT 100.00,
    driver_adjustment_ratio DECIMAL(5,2) NOT NULL DEFAULT 0.20,
    config_key VARCHAR(255) UNIQUE,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS pricing_temporary_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    multiplier DECIMAL(5,2) NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT 1,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_active_dates (is_active, start_date, end_date),
    INDEX idx_priority (priority)
);

CREATE TABLE IF NOT EXISTS driver_restrictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    restriction_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    reason VARCHAR(500),
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    appeal_status VARCHAR(255) NOT NULL DEFAULT 'NONE',
    appeal_material VARCHAR(1000),
    appeal_reason VARCHAR(500),
    appeal_submitted_at DATETIME(6),
    reviewer_id BIGINT,
    review_remark VARCHAR(500),
    reviewed_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_driver_id (driver_id),
    INDEX idx_status (status),
    INDEX idx_restriction_type (restriction_type),
    INDEX idx_driver_status (driver_id, status),
    INDEX idx_end_time (status, end_time)
);

CREATE TABLE IF NOT EXISTS driver_restriction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restriction_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    old_status VARCHAR(255),
    new_status VARCHAR(255) NOT NULL,
    remark VARCHAR(500) NOT NULL,
    created_at DATETIME(6),
    INDEX idx_restriction_id (restriction_id),
    FOREIGN KEY (restriction_id) REFERENCES driver_restrictions(id) ON DELETE CASCADE
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

INSERT IGNORE INTO routes (id, owner_id, start_location, end_location, start_time, return_time, days_of_week, seats, price_per_seat, average_distance_km, is_active, created_at, updated_at) VALUES
(1, 1, '望京SOHO', '中关村软件园', '08:00', '18:00', 'MON,TUE,WED,THU,FRI', 4, 15.00, 18.50, 1, NOW(), NOW()),
(2, 2, '天通苑', '望京SOHO', '07:30', '18:30', 'MON,TUE,WED,THU,FRI', 3, 10.00, 12.00, 1, NOW(), NOW()),
(3, 3, '回龙观', '西二旗', '08:30', '19:00', 'MON,WED,FRI', 2, 12.00, 10.00, 1, NOW(), NOW()),
(4, 1, '望京SOHO', '国贸CBD', '09:00', '18:00', 'TUE,THU', 3, 20.00, 22.00, 1, NOW(), NOW()),
(5, 4, '朝阳大悦城', '亦庄经济开发区', '07:00', '17:30', 'MON,TUE,WED,THU,FRI', 5, 25.00, 30.00, 1, NOW(), NOW());

INSERT IGNORE INTO carpools (id, route_id, driver_id, trip_date, available_seats, status, note, suggested_price_per_seat, final_price_per_seat, created_at) VALUES
(1, 1, 1, CURDATE(), 4, 'OPEN', '早上从望京出发，准时发车', 37.00, 37.00, NOW()),
(2, 2, 2, CURDATE(), 3, 'OPEN', '天通苑到望京，每天固定路线', 24.00, 24.00, NOW()),
(3, 3, 3, CURDATE(), 2, 'FULL', '回龙观到西二旗，已有乘客', 20.00, 20.00, NOW()),
(4, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, 'OPEN', '明天望京到国贸', 37.00, 37.00, NOW()),
(5, 5, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 5, 'OPEN', '朝阳到亦庄，周末也有', 60.00, 60.00, NOW());

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

INSERT IGNORE INTO pricing_configs (id, base_price_per_km, peak_hour_multiplier, off_peak_multiplier, heat_multiplier_base, heat_multiplier_per_booking, heat_multiplier_max, seat_scarcity_threshold, seat_scarcity_multiplier, distance_weight, min_price_per_seat, max_price_per_seat, driver_adjustment_ratio, config_key, created_at, updated_at) VALUES
(1, 2.50, 1.30, 0.90, 1.00, 0.0200, 1.50, 1, 1.15, 1.00, 5.00, 100.00, 0.20, 'DEFAULT', NOW(), NOW());

INSERT IGNORE INTO pricing_temporary_rules (id, name, description, start_date, end_date, start_time, end_time, multiplier, is_active, priority, created_at, updated_at) VALUES
(1, '春节假期加价', '春节假期出行高峰，价格上浮30%', DATE_ADD(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 37 DAY), NULL, NULL, 1.30, 1, 10, NOW(), NOW()),
(2, '国庆假期加价', '国庆节期间临时加价25%', DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 127 DAY), NULL, NULL, 1.25, 1, 10, NOW(), NOW()),
(3, '工作日早高峰特惠', '工作日早高峰促销折扣10%', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), '07:00', '08:00', 0.90, 1, 5, NOW(), NOW());

INSERT IGNORE INTO driver_restrictions (id, driver_id, restriction_type, status, reason, start_time, end_time, appeal_status, appeal_material, appeal_reason, appeal_submitted_at, reviewer_id, review_remark, reviewed_at, created_at, updated_at) VALUES
(1, 3, 'TOO_MANY_COMPLAINTS', 'ACTIVE', '近一个月内收到4次有效投诉（评分≤2），超过阈值3次', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'PENDING', '/uploads/appeal_evidence_1.pdf', '已有改善，请求解除限制', NOW(), NULL, NULL, NULL, NOW(), NOW()),
(2, 4, 'NO_SHOW', 'EXPIRED', '近一个月内出现1次爽约行为', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 'REJECTED', NULL, '当时有紧急情况', DATE_SUB(NOW(), INTERVAL 9 DAY), 9, '证据不充分，驳回申诉', DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), NOW());

INSERT IGNORE INTO driver_restriction_logs (id, restriction_id, operator_id, old_status, new_status, remark, created_at) VALUES
(1, 1, 3, NULL, 'ACTIVE', '近一个月内收到4次有效投诉（评分≤2），超过阈值3次', NOW()),
(2, 1, 3, 'ACTIVE', 'ACTIVE', '司机提交申诉：已有改善，请求解除限制', NOW()),
(3, 2, 4, NULL, 'ACTIVE', '近一个月内出现1次爽约行为', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 2, 4, 'ACTIVE', 'ACTIVE', '司机提交申诉：当时有紧急情况', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5, 2, 9, 'ACTIVE', 'EXPIRED', '管理员驳回申诉：证据不充分，驳回申诉', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 2, 0, 'ACTIVE', 'EXPIRED', '限制期限到期，自动解除', DATE_SUB(NOW(), INTERVAL 3 DAY));
