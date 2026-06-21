CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
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

INSERT IGNORE INTO users (id, username, email, password_hash, avatar, created_at, updated_at) VALUES
(1, 'zhangsan', 'zhangsan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(2, 'lisi', 'lisi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(3, 'wangwu', 'wangwu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(4, 'zhaoliu', 'zhaoliu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(5, 'sunqi', 'sunqi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(6, 'zhouba', 'zhouba@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(7, 'wujiu', 'wujiu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW()),
(8, 'zhengshi', 'zhengshi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NOW(), NOW());

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
