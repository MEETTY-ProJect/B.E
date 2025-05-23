package com.example.meetty.notification.controller;

import com.example.meetty.notification.dto.NotificationRequestDto;
import com.example.meetty.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {
    private final NotificationService notificationService;

    // 클라이언트가 /app/notify 로 메시지를 보내면 호출됨
    @MessageMapping("/notify")
    public void handleNotification(
            @Payload NotificationRequestDto notificationRequestDto,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes
    ) {
        Long receiverId = (Long) sessionAttributes.get("userId");

        if (receiverId == null) {
            log.warn("WebSocket 세션에서 userId를 찾을 수 없습니다.");
            return;
        }

        log.info("🔔 WebSocket 알림 요청 수신 - userId: {}, type: {}", receiverId, notificationRequestDto.getNotificationType());

        notificationService.notifyUser(
                receiverId,
                notificationRequestDto.getNotificationType(),
                notificationRequestDto.getContent(),
                notificationRequestDto.getUrl()
        );
    }
}
