package com.example.meetty.board.controller;

import com.example.meetty.board.service.InviteService;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;
    @Value("${frontend.study-room-detail-url}")
    private String frontendStudyRoomDetailBaseUrl;
    @Operation(summary = "스터디 그룹 초대 수락", description = "이메일 초대 링크를 통해 스터디 그룹 초대를 수락합니다.")
    @GetMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@RequestParam String token, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = currentUser.getUserId();
        Long acceptedRoomId = inviteService.acceptStudyGroupInvitation(token,currentUserId);
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendStudyRoomDetailBaseUrl)
                .pathSegment(acceptedRoomId.toString())
                .build().toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }
}
