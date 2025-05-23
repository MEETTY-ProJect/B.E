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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final StudyRoomRepository studyRoomRepository;
    private final StudyMembersRepository studyMembersRepository;
    private final UserRepository userRepository;

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
}