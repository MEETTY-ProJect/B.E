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

        Page<StudyRoomListCardResponse> responsePage = studyGroupPage.map(studyGroup -> {
            int currentMemberCount = studyMembersRepository.countByStudyRoomRoomIdAndStatus(studyGroup.getRoomId(), MemberStatus.ACTIVE);
            return new StudyRoomListCardResponse(studyGroup, currentMemberCount);
        });

        return new StudyRoomListResponse(responsePage);
    }


    @Transactional
    public StudyRoomResponse updateStudyGroup(Long id, UpdateStudyRoomRequest request,Long userId) {
        StudyRoomEntity studyGroup = studyRoomRepository.findById(id)
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
        StudyRoomEntity studyGroup = studyRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND,ErrorCode.STUDY_GROUP_NOT_FOUND.getMessage()));

        // 호스트만 삭제 가능
        if (!studyGroup.getHost().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS,ErrorCode.UNAUTHORIZED_STUDY_GROUP_ACCESS.getMessage());
        }

        studyRoomRepository.delete(studyGroup);
    }
}