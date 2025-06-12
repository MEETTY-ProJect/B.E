package com.example.meetty.chat.repository;

import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {

    //스터디방에 있는 메시지 조회
    List<ChatMessageResponseDto> findMessagesByRoomId(Long roomId, Long lastMessageId, int limit);


}
