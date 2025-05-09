package com.example.meetty.chat.repository;

import com.example.meetty.chat.dto.ChatMessageResponseDto;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessageResponseDto> findMessagesByRoomId(Long roomId, Long lastMessageId, int limit);

}
