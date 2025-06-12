package com.example.meetty.chat.controller;

import com.example.meetty.chat.dto.ChatMessageRequestDto;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.service.ChatMessageService;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "채팅관련 API")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅조회" , description = "채팅을 리미트 수만큼 최근 목록 조회, 스크롤 조회 가능")
    @GetMapping("/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponseDto>> getMessages(@PathVariable Long roomId,
                                                                 @RequestParam(required = false) Long lastMessageId,
                                                                 @RequestParam(defaultValue = "30") int limit,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatMessageResponseDto> messages = chatMessageService.getChatMessages(roomId,userDetails.getUserId(), lastMessageId, limit );
        return ApiResponse.success(messages);
    }


    //실시간 채팅으로 대체함.
    @Operation(summary = "채팅쓰기" , description = "채팅을 서버에 저장하는 API")
    @PostMapping("/{roomId}/messages")
    public ApiResponse<Void> saveMessage(@PathVariable Long roomId,
                                            @RequestBody ChatMessageRequestDto request,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatMessageService.saveMessage(roomId,userDetails.getUserId(), request);
        return ApiResponse.success(null);
    }
}
