package com.example.meetty.notification.dto;

import com.example.meetty.notification.entity.NotificationEntity;
import com.example.meetty.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private String content;
    private NotificationType notificationType;
    private String url;
    private LocalDateTime createdAt;

    public NotificationResponseDto(NotificationEntity notificationEntity) {
        this.id = notificationEntity.getNotificationId();
        this.content = notificationEntity.getContent();
        this.notificationType = notificationEntity.getNotificationType();
        this.url = notificationEntity.getUrl();
        this.createdAt = notificationEntity.getCreatedAt();
    }
}
