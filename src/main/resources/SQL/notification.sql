CREATE TABLE notifications (
                               notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               receiver_id BIGINT NOT NULL,
                               content VARCHAR(255),
                               notification_type VARCHAR(50),
                               url VARCHAR(255),
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at DATETIME
);