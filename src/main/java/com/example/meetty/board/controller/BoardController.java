package com.example.meetty.board.controller;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.dto.*;
import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.service.BoardService;
import com.example.meetty.global.config.auth.CustomUserDetails;
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
@RequestMapping("/api/board")
//@PreAuthorize("isAuthenticated()") -> 인증된 사용자만 접근하도록 만든 어노테이션, 지금 사용 안 할 예정
@Tag(name = "스터디 그룹 관련 API", description = "스터디 그룹 생성 및 조회 관련된 API")
public class BoardController {
    private final BoardService boardService;
    @Operation(summary = "스터디 그룹 생성", description = "새로운 스터디 그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<?> createStudyGroup( @Valid @RequestBody CreateRoomRequest request,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
//            if (request.getRoomName() != null && request.getRoomName().contains("금지어")) {
//                throw new AppException(ErrorCode.INVALID_INPUT_VALUE, "방 이름에 사용할 수 없는 단어가 포함되어 있습니다.");
//            }

            Long userId = currentUser.getUserId();
            StudyRoomResponse response = boardService.createStudyGroup(request,userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AppException e) {
            throw new AppException(ErrorCode.BINDING_RESULT_ERROR,ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }
    }
    @Operation(summary = "스터디 그룹 상세 조회", description = "특정 ID의 스터디 그룹 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyGroupById(@PathVariable Long id) {
        try {
            StudyRoomResponse response = boardService.getStudyGroupById(id);
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            throw new AppException(ErrorCode.BINDING_RESULT_ERROR,ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }
    }
    @Operation(summary = "스터디 그룹 목록 조회", description = "조건에 맞는 스터디 그룹 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getAllStudyGroups( @RequestParam(required = false) String roomName,
                                                @RequestParam(required = false) StudyPurpose purpose,
                                                @RequestParam(required = false) String region,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "15") int size) {

        try {
            StudyRoomListResponse<StudyRoomListCardResponse> response = boardService.getAllStudyGroups(roomName, purpose, region, page, size);
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            throw new AppException(ErrorCode.BINDING_RESULT_ERROR,ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }
    }
    @Operation(summary = "스터디 그룹 수정", description = "특정 ID의 스터디 그룹 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudyGroup( @PathVariable Long id,
                                               @Valid @RequestBody UpdateStudyRoomRequest request,
                                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long userId = currentUser.getUserId();
            StudyRoomResponse response = boardService.updateStudyGroup(id, request,userId);
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            throw new AppException(ErrorCode.BINDING_RESULT_ERROR,ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }
    }
    @Operation(summary = "스터디 그룹 삭제", description = "특정 ID의 스터디 그룹을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyGroup(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long userId = currentUser.getUserId();
            boardService.deleteStudyGroup(id,userId);
            return ResponseEntity.ok("스터디 그룹 삭제가 완료되었습니다.");
        } catch (AppException e) {
            throw new AppException(ErrorCode.BINDING_RESULT_ERROR,ErrorCode.BINDING_RESULT_ERROR.getMessage());
        }
    }
}

