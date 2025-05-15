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

CREATE TABLE `chat_rooms` (
                              `room_id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `room_name` varchar(100) NOT NULL,
                              `created_at` datetime DEFAULT current_timestamp(),
                              PRIMARY KEY (`room_id`)
);

CREATE TABLE `chat_messages` (
                                 `message_id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `room_id` bigint(20) NOT NULL,
                                 `user_id` bigint(20) NOT NULL,
                                 `message` varchar(250) NOT NULL,
                                 `created_at` datetime DEFAULT current_timestamp(),
                                 PRIMARY KEY (`message_id`),
                                 KEY `room_id` (`room_id`),
                                 KEY `user_id` (`user_id`),
                                 CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`room_id`) ON DELETE CASCADE,
                                 CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
)