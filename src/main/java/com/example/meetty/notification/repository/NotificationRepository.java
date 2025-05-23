package com.example.meetty.notification.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByReceiverOrderByCreatedAtDesc(UserEntity receiverId);
    Optional<NotificationEntity> findByNotificationIdAndReceiver(Long notificationId, UserEntity receiver);
    void deleteAllByReceiver(UserEntity receiver);
    List<NotificationEntity> findAllByReceiverAndIsReadFalse(UserEntity receiver);
}
