package com.example.meetty.notification.controller;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.notification.dto.NotificationRequestDto;
import com.example.meetty.notification.dto.NotificationResponseDto;
import com.example.meetty.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
@Tag(name = "알림 API", description = "스터디 입장 요청/승인과 같은 사용자 알림 관련 API")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "내 알림 목록 조회", description = "현재 로그인한 사용자의 알림 목록을 최신순으로 조회합니다.")
    @GetMapping("/v1/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        log.info("[GET] 알림 목록 조회 - userId: {}", userId);

        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(userEntity);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @Operation(summary = "알림 읽음 처리 API", description = "경로에 설정한 notification_id를 가진 알림을 '읽음'으로 변경합니다.")
    @PatchMapping("/v1/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAsRead(notificationId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("알림 읽음 처리 완료"));
    }

    @Operation(summary = "알림 삭제 API", description = "경로에 설정한 notification_id를 가진 알림을 삭제합니다.")
    @DeleteMapping("/v1/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteNotification(notificationId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("알림 삭제 완료"));
    }

    @Operation(summary = "전체 알림 삭제 API", description = "사용자의 모든 알림을 삭제합니다.")
    @DeleteMapping("/v1/notifications")
    public ResponseEntity<ApiResponse<String>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteAllNotifications(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("전체 알림 삭제 완료"));
    }
}
