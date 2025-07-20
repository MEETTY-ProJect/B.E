package com.example.meetty.chat.dto;


import com.example.meetty.board.dto.StudyRoomMemberListResponseDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomResponseDto {
    private List<ChatMessageResponseDto> messages;
    private List<StudyRoomMemberListResponseDto> members;

    @QueryProjection
    public ChatRoomResponseDto(List<ChatMessageResponseDto> messages, List<StudyRoomMemberListResponseDto> members) {
        this.messages = messages;
        this.members = members;
    }
}
