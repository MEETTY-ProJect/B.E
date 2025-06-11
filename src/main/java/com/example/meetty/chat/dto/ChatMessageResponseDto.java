package com.example.meetty.chat.dto;

import com.example.meetty.chat.entity.ChatMessage;
import com.example.meetty.chat.entity.QChatMessage;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponseDto {

    private Long messageId;
    private Long roomId;
    private Long userId;
    private String username;
    private String profileImage;
    private String message;
    private LocalDateTime createdAt;

    @QueryProjection
    public ChatMessageResponseDto(Long messageId, Long roomId, Long userId, String username, String profileImage, String message, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.message = message;
        this.createdAt = createdAt;
    }



    public ChatMessageResponseDto(ChatMessage chatMessage) {
        this.messageId = chatMessage.getMessageId();
        this.roomId = chatMessage.getRoom().getRoomId();
        this.userId = chatMessage.getSender().getUserId();
        this.username = chatMessage.getSender().getUsername();
        this.profileImage = chatMessage.getSender().getUserImageEntity().getUrl();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }



}
