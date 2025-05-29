package com.example.meetty.board.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.board.dto.*;
import com.example.meetty.board.entity.MemberStatus;
import com.example.meetty.board.entity.StudyMembersEntity;
import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.repository.StudyRoomRepository;
import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {
    private final StudyRoomRepository studyRoomRepository;
    private final StudyMembersRepository studyMembersRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public StudyRoomResponse createStudyGroup(CreateRoomRequest request,Long userId) {

        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        StudyRoomEntity studyGroup = StudyRoomEntity.builder()
                .roomName(request.getRoomName())
                .introduction(request.getIntroduction())
                .capacity(request.getCapacity())
                .purpose(request.getPurpose())
                .region(request.getRegion())
                .host(currentUser)
                .hostName(currentUser.getUsername())
                .createdAt(LocalDateTime.now())
                .build();

        StudyRoomEntity savedStudyGroup = studyRoomRepository.save(studyGroup);

        StudyMembersEntity hostMember = StudyMembersEntity.builder()
                .studyRoom(savedStudyGroup)
                .member(currentUser)
                .joinedAt(LocalDateTime.now())
                .status(MemberStatus.ACTIVE)
                .build();

        studyMembersRepository.save(hostMember);

        int currentMemberCount = 1;
        return new StudyRoomResponse(savedStudyGroup, currentMemberCount);
    }

    public StudyRoomResponse getStudyGroupById(Long id) {
        StudyRoomEntity studyGroup = studyRoomRepository.findByIdWithHost(id)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        int currentMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(studyGroup.getRoomId(), MemberStatus.ACTIVE);

        return new StudyRoomResponse(studyGroup, currentMemberCount);
    }

    @Transactional(readOnly = true)
    public StudyRoomListResponse<StudyRoomListCardResponse> getAllStudyGroups(
            String roomName, StudyPurpose purpose, String region, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<StudyRoomEntity> studyGroupPage;

        boolean hasName = StringUtils.hasText(roomName);
        boolean hasPurpose = purpose != null;
        boolean hasRegion = StringUtils.hasText(region);

        if (hasName && hasPurpose && hasRegion) {
            studyGroupPage = studyRoomRepository.findByNameContainingIgnoreCaseAndPurposeAndRegionWithHost(roomName, purpose, region, pageable);
        } else if (hasName && hasPurpose) {
            studyGroupPage = studyRoomRepository.findByNameContainingIgnoreCaseAndPurposeWithHost(roomName, purpose, pageable);
        } else if (hasName && hasRegion) {
            studyGroupPage = studyRoomRepository.findByNameContainingIgnoreCaseAndRegionWithHost(roomName, region, pageable);
        } else if (hasPurpose && hasRegion) {
            studyGroupPage = studyRoomRepository.findByPurposeAndRegionWithHost(purpose, region, pageable);
        } else if (hasName) {
            studyGroupPage = studyRoomRepository.findByNameContainingIgnoreCaseWithHost(roomName, pageable);
        } else if (hasPurpose) {
            studyGroupPage = studyRoomRepository.findByPurposeWithHost(purpose, pageable);
        } else if (hasRegion) {
            studyGroupPage = studyRoomRepository.findByRegionWithHost(region, pageable);
        } else {
            studyGroupPage = studyRoomRepository.findAllWithHost(pageable);
        }

        List<Long> roomIds = studyGroupPage.getContent().stream()
                .map(StudyRoomEntity::getRoomId)
                .collect(Collectors.toList());

        // 만약 현재 페이지에 스터디 룸이 없다면 (빈 페이지), 카운트 쿼리를 실행할 필요가 없습니다.
        if (roomIds.isEmpty()) {
            return new StudyRoomListResponse(studyGroupPage.map(studyGroup ->
                    new StudyRoomListCardResponse(studyGroup, 0)
            ));
        }

        List<Object[]> memberCounts = studyMembersRepository.countActiveMembersByRoomIds(roomIds, MemberStatus.ACTIVE);

        Map<Long, Integer> memberCountMap = memberCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],     // 스터디 룸 ID
                        row -> ((Long) row[1]).intValue() // 멤버 수
                ));

        Page<StudyRoomListCardResponse> responsePage = studyGroupPage.map(studyGroup -> {
            int currentMemberCount = memberCountMap.getOrDefault(studyGroup.getRoomId(), 0);
            return new StudyRoomListCardResponse(studyGroup, currentMemberCount);
        });

        return new StudyRoomListResponse(responsePage);
    }


    @Transactional
    public StudyRoomResponse updateStudyGroup(Long id, UpdateStudyRoomRequest request,Long userId) {
        StudyRoomEntity studyGroup = studyRoomRepository.findByIdWithHost(id)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        if (!studyGroup.getHost().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS,ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }

        studyGroup.updateInfo(request.getName(), request.getIntroduction(), request.getPurpose());

        int currentMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(studyGroup.getRoomId(), MemberStatus.ACTIVE);

        return new StudyRoomResponse(studyGroup, currentMemberCount);
    }

    @Transactional
    public void deleteStudyGroup(Long id,Long userId) {
        StudyRoomEntity studyGroup = studyRoomRepository.findByIdWithHost(id)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        // 호스트만 삭제 가능
        if (!studyGroup.getHost().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS,ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS.getMessage());
        }

        int totalActiveMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(studyGroup.getRoomId(), MemberStatus.ACTIVE);

        if (totalActiveMemberCount > 1) {
            throw new AppException(ErrorCode.STUDY_GROUP_DELETE_FAILED_HAS_MEMBERS, ErrorCode.STUDY_GROUP_DELETE_FAILED_HAS_MEMBERS.getMessage());
        }

        studyRoomRepository.delete(studyGroup);
    }
// ----------------------------------------------------------------------------------------------------------------------
    @Transactional
    public void requestJoinStudyGroup(Long roomId, Long userId) {
        StudyRoomEntity studyRoom = studyRoomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND, ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage()));

        Optional<StudyMembersEntity> existingMember = studyMembersRepository.findByStudyRoomRoomIdAndMemberUserId(roomId, userId);
        if (existingMember.isPresent()) {
            throw new AppException(ErrorCode.ALREADY_STUDY_GROUP_MEMBER, ErrorCode.ALREADY_STUDY_GROUP_MEMBER.getMessage());
        }

        // 최대 인원 확인
        int currentActiveMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(roomId, MemberStatus.ACTIVE);
        if (currentActiveMemberCount >= studyRoom.getCapacity()) {
            throw new AppException(ErrorCode.STUDY_GROUP_CAPACITY_FULL, ErrorCode.STUDY_GROUP_CAPACITY_FULL.getMessage());
        }

        StudyMembersEntity pendingMember = StudyMembersEntity.builder()
                .studyRoom(studyRoom)
                .member(user)
                .joinedAt(LocalDateTime.now())
                .status(MemberStatus.PENDING)
                .build();

        studyMembersRepository.save(pendingMember);

    }

    @Transactional // DB 조회 등을 포함하므로 트랜잭션 유지. 이메일/Redis 실패 시 롤백 여부는 정책에 따라 달라질 수 있음.
    public void inviteStudyGroupMember(Long roomId, String targetUserNickname, Long hostUserId) {
        StudyRoomEntity studyRoom = studyRoomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND, ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        if (!studyRoom.getHost().getUserId().equals(hostUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS, ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS.getMessage());
        }

        UserEntity targetUser = userRepository.findByUsername(targetUserNickname)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, targetUserNickname + " 닉네임을 가진 회원을 찾을 수 없습니다."));

        Optional<StudyMembersEntity> existingMember = studyMembersRepository.findByStudyRoomRoomIdAndMemberUserId(roomId, targetUser.getUserId());
        if (existingMember.isPresent()) {
            throw new AppException(ErrorCode.ALREADY_STUDY_GROUP_MEMBER, targetUserNickname + " 회원은 이미 스터티 그룹 멤버입니다.");
        }

        int currentActiveMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(roomId, MemberStatus.ACTIVE);
        if (currentActiveMemberCount >= studyRoom.getCapacity()) {
            throw new AppException(ErrorCode.STUDY_GROUP_CAPACITY_FULL, "스터디 그룹 인원이 가득 찼습니다. 더 이상 초대할 수 없습니다.");
        }

        String invitationToken = generateInvitationToken();
        String tokenKey = "study_invite:" + invitationToken;
        String tokenValue = roomId + ":" + targetUser.getUserId();
        long expirationTime = 24;
        TimeUnit expirationUnit = TimeUnit.HOURS;

        try {
            redisTemplate.opsForValue().set(tokenKey, tokenValue, expirationTime, expirationUnit);
            log.info("Redis에 스터디 그룹 초대 토큰 저장 완료: {}", tokenKey);
        } catch (Exception e) {
            log.error("Redis 토큰 저장 실패: {}", tokenKey, e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "초대 토큰 저장에 실패했습니다. 다시 시도해주세요.");
        }

        String subject = "스터디 그룹 [" + studyRoom.getRoomName() + "] 초대 메일";
        String htmlBody = "<html><body>"
                + "<p>" + targetUserNickname + "님을 스터디 그룹 [" + studyRoom.getRoomName() + "]에 초대합니다.</p>"
                + "<p>아래 링크를 클릭하여 초대를 수락해주세요.</p>"
                + "<p><a href='{invitationLink}'>초대 수락하기</a></p>"
                + "<p>이 링크는 " + expirationTime + " " + expirationUnit.toString().toLowerCase() + " 후 만료됩니다.</p>"
                + "</body></html>";

                // 이미지 추가 시 <img src='cid:imageId'> 등 사용
        try {
            emailService.sendStudyInviteEmail(
                    targetUser.getEmail(),
                    subject,
                    htmlBody,
                    invitationToken
            );
            log.info("{} ({}) 회원에게 스터디 그룹 초대 메일 (HTML) 발송 완료", targetUserNickname, targetUser.getEmail());
        } catch (Exception e) {
            log.error("초대 메일 발송 실패: {}", targetUser.getEmail(), e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED, ErrorCode.EMAIL_SEND_FAILED.getMessage());
        }
    }

    @Transactional
    public void updateStudyGroupMemberStatus(Long roomId, Long memberId, MemberStatus newStatus, Long hostUserId) {
        StudyRoomEntity studyRoom = studyRoomRepository.findByIdWithHost(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND, ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        if (!studyRoom.getHost().getUserId().equals(hostUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS, ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS.getMessage());
        }

        StudyMembersEntity memberToUpdate = studyMembersRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_MEMBER_NOT_FOUND, ErrorCode.STUDY_GROUP_MEMBER_NOT_FOUND.getMessage()));


        if (!memberToUpdate.getStudyRoom().getRoomId().equals(roomId)) {
            throw new AppException(ErrorCode.STUDY_GROUP_MEMBER_MISMATCH, ErrorCode.STUDY_GROUP_MEMBER_MISMATCH.getMessage());
        }

        if (memberToUpdate.getStatus() == MemberStatus.PENDING && newStatus == MemberStatus.ACTIVE) {
            int currentActiveMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(roomId, MemberStatus.ACTIVE);
            if (currentActiveMemberCount >= studyRoom.getCapacity()) {
                throw new AppException(ErrorCode.STUDY_GROUP_CAPACITY_FULL, ErrorCode.STUDY_GROUP_CAPACITY_FULL.getMessage());
            }
        }

        memberToUpdate.setStatus(newStatus);
    }

    public List<StudyRoomListCardResponse> getMyStudyRooms(Long hostUserId) {
        UserEntity currentUser = userRepository.findById(hostUserId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        List<StudyMembersEntity> activeMemberships = studyMembersRepository.findByMemberAndStatus(currentUser, MemberStatus.ACTIVE);

        if (activeMemberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roomIds = activeMemberships.stream()
                .map(membership -> membership.getStudyRoom().getRoomId())
                .collect(Collectors.toList());

        List<Object[]> memberCounts = studyMembersRepository.countActiveMembersByRoomIds(roomIds, MemberStatus.ACTIVE);

        Map<Long, Integer> memberCountMap = memberCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
        List<StudyRoomListCardResponse> studyRoomList = activeMemberships.stream()
                .map(membership -> {
                    StudyRoomEntity studyRoom = membership.getStudyRoom();
                    int currentMemberCount = memberCountMap.getOrDefault(studyRoom.getRoomId(), 0);
                    return new StudyRoomListCardResponse(studyRoom, currentMemberCount);
                })
                .collect(Collectors.toList());
        return studyRoomList;
    }

     @Transactional(readOnly = true)
    public boolean isStudyGroupMember(Long roomId, Long userId) {
        Optional<StudyMembersEntity> member = studyMembersRepository.findByStudyRoomRoomIdAndMemberUserIdAndStatus(roomId, userId, MemberStatus.ACTIVE);
        return member.isPresent();
    }

    private String generateInvitationToken() {
        //return java.util.UUID.randomUUID().toString(); // UUID 사용 시
        return RandomStringUtils.randomAlphanumeric(32); // 32자리의 영숫자 랜덤 문자열
    }



}
