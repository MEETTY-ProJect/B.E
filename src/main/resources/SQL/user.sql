CREATE TABLE users(
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(20) UNIQUE NOT NULL,
    address VARCHAR(100),
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    is_verified TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL
);

CREATE TABLE user_images(
    image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    url VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL
);