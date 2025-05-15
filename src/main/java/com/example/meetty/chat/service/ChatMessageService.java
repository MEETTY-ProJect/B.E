package com.example.meetty.chat.service;


import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.board.repository.StudyRoomRepository;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.entity.ChatMessage;
import com.example.meetty.chat.repository.ChatMessageRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
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
    private final StudyMembersRepository studyMembersRepository;

    //채팅 조회
    public List<ChatMessageResponseDto> getChatMessages(Long roomId, Long lastMessageId, int limit) {
        return chatMessageRepository.findMessagesByRoomId(roomId, lastMessageId, limit);
    }

    @Transactional
    public void saveMessage(Long roomId, Long userId, String message) {
        //참여자 검증
        boolean isMember = studyMembersRepository.existsByStudyRoom_RoomIdAndMember_UserId(roomId,userId);
        if (!isMember) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_ROOM_CHAT, ErrorCode.UNAUTHORIZED_STUDY_ROOM_CHAT.getMessage());
        }


        //메시지 저장
        UserEntity user = UserEntity.builder().userId(userId).build();
        StudyRoomEntity room = StudyRoomEntity.builder().roomId(roomId).build();

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user)
                .room(room)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);

    }




}
