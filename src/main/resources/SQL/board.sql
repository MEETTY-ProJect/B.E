CREATE TABLE study_rooms (
                             room_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             room_name VARCHAR(100) NOT NULL,
                             reason TEXT NOT NULL,
                             capacity INT NOT NULL,
                             purpose VARCHAR(100) NOT NULL,
                             region VARCHAR(100),
                             created_at DATETIME NOT NULL,
                             host_user_id BIGINT NOT NULL,
                             host_name VARCHAR(100) NOT NULL
);

CREATE TABLE study_room_members (
                                    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    room_id BIGINT NOT NULL,
                                    user_id BIGINT NOT NULL,
                                    joined_at DATETIME NOT NULL,
                                    status VARCHAR(20) NOT NULL,
                                    UNIQUE KEY `uk_room_member` (`room_id`, `user_id`)
);

CREATE TABLE join_requests (
                               request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               room_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               token VARCHAR(50) NOT NULL UNIQUE,
                               expires_at DATETIME NOT NULL,
                               created_at DATETIME NOT NULL
);

