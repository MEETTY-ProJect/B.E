package com.example.meetty.board.controller;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.dto.*;
import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.service.BoardService;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/board")
@Tag(name = "스터디 그룹 관련 API", description = "스터디 그룹 생성 및 조회 관련된 API")
public class BoardController {
    private final BoardService boardService;
    @Operation(summary = "스터디 그룹 생성", description = "새로운 스터디 그룹을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StudyRoomResponse>> createStudyGroup(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUserId();
        StudyRoomResponse response = boardService.createStudyGroup(request, userId);
        ApiResponse<StudyRoomResponse> successResponse = ApiResponse.success(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }
    @Operation(summary = "스터디 그룹 상세 조회", description = "특정 ID의 스터디 그룹 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyRoomResponse>> getStudyGroupById(@PathVariable Long id) {
            StudyRoomResponse response = boardService.getStudyGroupById(id);
            ApiResponse<StudyRoomResponse> successResponse = ApiResponse.success(response);
            return ResponseEntity.ok(successResponse);
    }
    @Operation(summary = "스터디 그룹 목록 조회", description = "조건에 맞는 스터디 그룹 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<StudyRoomListResponse<StudyRoomListCardResponse>>> getAllStudyGroups( @RequestParam(required = false) String roomName,
                                                @RequestParam(required = false) StudyPurpose purpose,
                                                @RequestParam(required = false) String region,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "15") int size) {
            StudyRoomListResponse<StudyRoomListCardResponse> response = boardService.getAllStudyGroups(roomName, purpose, region, page, size);
            ApiResponse<StudyRoomListResponse<StudyRoomListCardResponse>> successResponse = ApiResponse.success(response);
            return ResponseEntity.ok(successResponse);
    }
    @Operation(summary = "스터디 그룹 수정", description = "특정 ID의 스터디 그룹 정보를 수정합니다.")
    @PutMapping("/modify/{id}")
    public ResponseEntity<ApiResponse<StudyRoomResponse>> updateStudyGroup( @PathVariable Long id,
                                               @Valid @RequestBody UpdateStudyRoomRequest request,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
            Long userId = currentUser.getUserId();
            StudyRoomResponse response = boardService.updateStudyGroup(id, request,userId);
            ApiResponse<StudyRoomResponse> successResponse = ApiResponse.success(response);
            return ResponseEntity.ok(successResponse);
    }

    @Operation(summary = "스터디 그룹 삭제", description = "특정 ID의 스터디 그룹을 삭제합니다.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStudyGroup(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUserId();
        boardService.deleteStudyGroup(id,userId);
        return ResponseEntity.ok(ApiResponse.success("스터디 그룹 삭제가 완료되었습니다."));
    }

    @Operation(summary = "스터디 그룹 참가 요청", description = "게스트 회원이 스터디 그룹에 참가를 요청합니다.")
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<String>> requestJoinStudyGroup(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getUserId();
        boardService.requestJoinStudyGroup(roomId, userId);
        // 참가 요청 성공 시 메시지 반환
        return ResponseEntity.ok(ApiResponse.success("스터디 그룹 참가 요청이 완료되었습니다."));
    }

    @Operation(summary = "스터디 그룹 참가 요청 승인/거절", description = "호스트가 게스트 회원의 참가 요청을 승인하거나 거절합니다. memberId는 StudyMembersEntity의 ID입니다.")
    @PutMapping("/{roomId}/members/{memberId}")
    public ResponseEntity<ApiResponse<String>> updateStudyGroupMemberStatus(
            @PathVariable Long roomId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long hostUserId = currentUser.getUserId();
        boardService.updateStudyGroupMemberStatus(roomId, memberId, request.getStatus(), hostUserId);
        return ResponseEntity.ok(ApiResponse.success("스터디 그룹 멤버 상태가 성공적으로 업데이트되었습니다."));
    }

    @Operation(summary = "스터디 그룹 멤버 초대", description = "호스트가 다른 회원을 스터디 그룹에 초대합니다. (초대 링크 발송 로직은 주석 처리)")
    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ApiResponse<String>> inviteStudyGroupMember(
            @PathVariable Long roomId,
            @Valid @RequestBody JoinRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long hostUserId = currentUser.getUserId();
        boardService.inviteStudyGroupMember(roomId, request.getTargetUserNickname(), hostUserId);
        return ResponseEntity.ok(ApiResponse.success(request.getTargetUserNickname() + " 회원에게 스터디 그룹 초대 메일이 발송되었습니다."));
    }

    // 스터디 그룹 멤버십 확인 로직 (API Gateway 또는 필터 레벨에서 구현될 수 있음)
    // 예: 특정 엔드포인트 진입 시 멤버인지 확인하는 인터셉터/필터 적용
}

