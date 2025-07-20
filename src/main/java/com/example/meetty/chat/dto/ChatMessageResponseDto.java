package com.example.meetty.chat.dto;

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

    /**
    QueryProjection을 사용하면 쿼리 결과를 엔티티로 변환 -> DTO로 다시 변환하는 과정을 생략할수 있음.
     jpa가 바로 DTO로 결롸를 채워줌
     그러기 위해선 repository에 custom과 impl로 쿼리문을 따로 만들어야한다.
     */
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
}
