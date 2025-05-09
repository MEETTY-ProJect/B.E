package com.example.meetty.chat.service;


import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.entity.ChatMessage;
import com.example.meetty.chat.entity.ChatRooms;
import com.example.meetty.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    //채팅 조회
    public List<ChatMessageResponseDto> getChatMessages(Long roomId, Long lastMessageId, int limit) {
        return chatMessageRepository.findMessagesByRoomId(roomId, lastMessageId, limit);
    }

    @Transactional
    public void saveMessage(Long roomId, Long userId, String message) {
        //userId, roomId는 이미 스터디방에 들어올때 검증되어 들어옴

        UserEntity user = UserEntity.builder().userId(userId).build();
        ChatRooms room = ChatRooms.builder().roomId(roomId).build();

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user)
                .room(room)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);

    }




}
