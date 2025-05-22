package com.example.meetty.notification.dto;

import com.example.meetty.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class NotificationRequestDto {
    @Schema(description = "알림 타입 (예: STUDY_REQUEST, MESSAGE 등)", example = "STUDY_REQUEST")
    private NotificationType notificationType;

    @Schema(description = "알림 내용", example = "스터디 입장 요청이 도착했습니다.")
    private String content;

    @Schema(description = "클릭 시 이동할 URL", example = "/study/123")
    private String url;
}
