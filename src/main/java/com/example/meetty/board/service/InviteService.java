package com.example.meetty.board.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.board.entity.MemberStatus;
import com.example.meetty.board.entity.StudyMembersEntity;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.board.repository.StudyRoomRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InviteService {
    private final StudyRoomRepository studyRoomRepository;
    private final StudyMembersRepository studyMembersRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;
    @Transactional
    public Long acceptStudyGroupInvitation(String token, Long currentUserId) {
        String tokenKey = "study_invite:" + token;
        String tokenValue = (String) redisTemplate.opsForValue().get(tokenKey);

        if (tokenValue == null) {
            throw new AppException(ErrorCode.INVALID_INVITATION_TOKEN, ErrorCode.INVALID_INVITATION_TOKEN.getMessage());
        }

        // 토큰 값 파싱 (roomId:userId 형태)
        String[] parts = tokenValue.split(":");
        if (parts.length != 2) {
            throw new AppException(ErrorCode.INVITE_TOKEN_INVALID, ErrorCode.INVITE_TOKEN_INVALID.getMessage());
        }
        Long roomId = Long.parseLong(parts[0]);
        Long targetUserId = Long.parseLong(parts[1]);

        if (!currentUserId.equals(targetUserId)) {
            redisTemplate.delete(tokenKey);
            throw new AppException(ErrorCode.UNAUTHORIZED_INVITE_TOKEN,ErrorCode.UNAUTHORIZED_INVITE_TOKEN.getMessage() ); }

        StudyRoomEntity studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND, ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage()));

        // 이미 멤버인지 다시 확인 (혹시 그 사이에 다른 방법으로 가입했을 경우)
        Optional<StudyMembersEntity> existingMember = studyMembersRepository.findByStudyRoomRoomIdAndMemberUserId(roomId, targetUser.getUserId());
        if (existingMember.isPresent()) {
            if (existingMember.get().getStatus() != MemberStatus.ACTIVE) {
                existingMember.get().setStatus(MemberStatus.ACTIVE);
                studyMembersRepository.save(existingMember.get());
                redisTemplate.delete(tokenKey);
                return roomId;
            }
            redisTemplate.delete(tokenKey);
            throw new AppException(ErrorCode.ALREADY_STUDY_GROUP_MEMBER, targetUser.getUsername() + " 회원은 이미 스터디 그룹 멤버입니다.");
        }


        int currentActiveMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(roomId, MemberStatus.ACTIVE);
        if (currentActiveMemberCount >= studyRoom.getCapacity()) {
            redisTemplate.delete(tokenKey);
            throw new AppException(ErrorCode.STUDY_GROUP_CAPACITY_FULL, ErrorCode.STUDY_GROUP_CAPACITY_FULL.getMessage());
        }


        StudyMembersEntity newMember = StudyMembersEntity.builder()
                .studyRoom(studyRoom)
                .member(targetUser)
                .joinedAt(LocalDateTime.now())
                .status(MemberStatus.ACTIVE)
                .build();

        studyMembersRepository.save(newMember);

        redisTemplate.delete(tokenKey);
        return roomId;
    }
}
