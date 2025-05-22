package com.example.meetty.notification.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.notification.dto.NotificationResponseDto;
import com.example.meetty.notification.entity.NotificationEntity;
import com.example.meetty.notification.entity.NotificationType;
import com.example.meetty.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 알림을 저장하고 WebSocket으로 전송하는 핵심 메서드
    public void notifyUser(Long receiverId, NotificationType notificationType, String content, String url) {
        // 1. 수신자 조회
        UserEntity receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자 정보를 찾을 수 없습니다."));

        // 2. 알림 생성 및 저장
        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .notificationType(notificationType)
                .content(content)
                .url(url)
                .build();

        notificationRepository.save(notification);

        log.info("💾 [NotifyService] 알림 저장 완료 - receiverId: {}, content: {}", receiverId, content);

        // 3. WebSocket 전송 (ex: /topic/user/3)
        simpMessagingTemplate.convertAndSend(
                "/topic/user/" + receiverId,
                new NotificationResponseDto(notification)
        );

        log.info("🔔 [{}] 알림 전송 완료 to userId={}", notificationType, receiverId);
    }

    // 알림 메시지 조회
    public List<NotificationResponseDto> getUserNotifications(UserEntity receiver) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver).stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }
}
