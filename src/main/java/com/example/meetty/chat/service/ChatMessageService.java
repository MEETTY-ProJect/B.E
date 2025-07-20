package com.example.meetty.chat.service;


import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.board.dto.StudyRoomMemberListResponseDto;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.board.repository.StudyRoomRepository;
import com.example.meetty.chat.dto.ChatMessageRequestDto;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.dto.ChatRoomResponseDto;
import com.example.meetty.chat.entity.ChatMessage;
import com.example.meetty.chat.repository.ChatMessageRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.image.entity.UserImageEntity;
import com.example.meetty.image.repository.UserImageRepository;
import com.example.meetty.image.uploader.GcpImageUploader;
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
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final GcpImageUploader gcpImageUploader;


    //채팅 조회
    public ChatRoomResponseDto getChatMessages(Long roomId, Long userId, Long lastMessageId, int limit) {

        //스터디 그룹 존재 여부 검증
        if (!studyRoomRepository.existsById(roomId)) {
            throw new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND);
        }

        //스터디 그룹 멤버 여부 검증
        if (!studyMembersRepository.existsByStudyRoom_RoomIdAndMember_UserId(roomId, userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS);
        }

        //1.채팅 메시지 조회
        List<ChatMessageResponseDto> chatMessages = chatMessageRepository.findMessagesByRoomId(roomId,lastMessageId,limit);

        //2. 멤버 리스트 조회
        List<StudyRoomMemberListResponseDto> members = studyMembersRepository.findByStudyRoomMember_RoomId(roomId);

        //3. 통합해서 응답
        return new ChatRoomResponseDto(chatMessages,members);


    }

    @Transactional
    public ChatMessageResponseDto saveMessage(Long roomId, Long userId, ChatMessageRequestDto dto) {

        //참여자 검증
        boolean isMember = studyMembersRepository.existsByStudyRoom_RoomIdAndMember_UserId(roomId,userId);

        if (!isMember) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_ROOM_CHAT);
        }


        /** 메시지저장 **/
        // 유저 조회 (UserEntity 전체 가져오기)
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // 스터디룸 조회 (StudyRoomEntity 전체 가져오기)
        StudyRoomEntity room = studyRoomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        // 메시지 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user)
                .room(room)
                .message(dto.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        // 프로필 이미지 단방향 조회 방식으로 수정
        UserImageEntity image = userImageRepository.findByUserEntity(user);
        String profileImage = image != null ? image.getUrl() : gcpImageUploader.getDefaultImageUrl();

        //저장  + 응답 변환
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 저장된 메시지를 ResponseDto로 변환
        return new ChatMessageResponseDto(
                savedMessage.getMessageId(),
                room.getRoomId(),
                user.getUserId(),
                user.getUsername(),
                profileImage,
                savedMessage.getMessage(),
                savedMessage.getCreatedAt()
        );
    }




}
