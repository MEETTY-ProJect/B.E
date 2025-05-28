package com.example.meetty.notification.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.notification.dto.NotificationResponseDto;
import com.example.meetty.notification.entity.NotificationEntity;
import com.example.meetty.notification.entity.NotificationType;
import com.example.meetty.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
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

    // 모든 알림 조회
    public List<NotificationResponseDto> getUserNotifications(UserEntity receiver) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(receiver).stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }


    // 지정한 알림을 '읽음'상태로 전환
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        UserEntity receiver = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        NotificationEntity notificationEntity = notificationRepository.findByNotificationIdAndReceiver(notificationId, receiver).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND_NOTIFICATION)
        );

        if (!notificationEntity.isRead()) {
            notificationEntity.markAsRead();
        }
    }

    // 지정한 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        UserEntity receiver = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        NotificationEntity notificationEntity = notificationRepository.findByNotificationIdAndReceiver(notificationId, receiver).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND_NOTIFICATION)
        );

        notificationRepository.delete(notificationEntity);
    }

    // 모든 알림 삭제
    @Transactional
    public void deleteAllNotifications(Long userId) {
        UserEntity receiver = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        notificationRepository.deleteAllByReceiver(receiver);
    }
}
