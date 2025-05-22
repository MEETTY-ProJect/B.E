package com.example.meetty.notification.event;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.notification.dto.NotificationResponseDto;
import com.example.meetty.notification.entity.NotificationEntity;
import com.example.meetty.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        // 1. WebSocket 세션에서 HandshakeInterceptor가 저장한 attributes 추출
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();

        if (attributes == null) {
            log.warn("❌ [WebSocket] SessionAttributes가 null입니다 - 연결 거부");
            return;
        }

        // 2. 인증된 사용자 ID 꺼내기 (HandshakeInterceptor에서 저장됨)
        Long userId = (Long) attributes.get("userId");
        if (userId == null) {
            log.warn("❌ [WebSocket] Session에 userId 없음 - 인증된 사용자 아님");
            return;
        }

        // 3. 사용자 정보 조회
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 4. 해당 사용자의 읽지 않은 알림 조회
        List<NotificationEntity> unread = notificationRepository.findAllByReceiverAndIsReadFalse(userEntity);

        // 5. 알림을 WebSocket(STOMP)으로 전송
        for (NotificationEntity notificationEntity : unread) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/user/" + userId,
                    new NotificationResponseDto(notificationEntity)
            );
        }

        log.info("📬 [WebSocket] 재접속 → 미확인 알림 {}개 전송 완료 (userId={})", unread.size(), userId);
    }
}
