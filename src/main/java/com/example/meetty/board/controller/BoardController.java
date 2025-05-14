package com.example.meetty.board.controller;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.dto.*;
import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.service.BoardService;
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
    // 스터디 그룹 생성
    @PostMapping
    public ResponseEntity<StudyRoomResponse> createStudyGroup(
            @Valid @RequestBody CreateRoomRequest request, @AuthenticationPrincipal UserEntity currentUser) {
        StudyRoomResponse response = boardService.createStudyGroup(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 스터디 그룹 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<StudyRoomResponse> getStudyGroupById(@PathVariable Long id) {
        StudyRoomResponse response = boardService.getStudyGroupById(id);
        return ResponseEntity.ok(response);
    }

    // 스터디 그룹 목록 조회 (검색 및 필터링, 페이징)
    @GetMapping
    public ResponseEntity<StudyRoomListResponse<StudyRoomListCardResponse>> getAllStudyGroups(
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) StudyPurpose purpose,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        StudyRoomListResponse<StudyRoomListCardResponse> response = boardService.getAllStudyGroups(roomName, purpose, region, page, size);
        return ResponseEntity.ok(response);
    }

    // 스터디 그룹 수정
    @PutMapping("/{id}")
    public ResponseEntity<StudyRoomResponse> updateStudyGroup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyRoomRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        StudyRoomResponse response = boardService.updateStudyGroup(id, request,currentUser);
        return ResponseEntity.ok(response);
    }

    // 스터디 그룹 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyGroup(@PathVariable Long id,@AuthenticationPrincipal UserEntity currentUser) {
        boardService.deleteStudyGroup(id,currentUser);
        return ResponseEntity.noContent().build();
    }

}
