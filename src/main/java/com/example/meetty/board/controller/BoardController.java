package com.example.meetty.board.controller;

import com.example.meetty.board.dto.CreateGroupDTO;
import com.example.meetty.board.dto.JoinRequestDTO;
import com.example.meetty.board.entity.StudyGroupEntity;
import com.example.meetty.board.entity.StudyMembersEntity;
import com.example.meetty.board.service.BoardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/board")
@Tag(name = "스터디 그룹 관련 API", description = "스터디 그룹 생성 및 조회 관련된 API")
public class BoardController {
    private final BoardService boardService;
    @PostMapping
    public ResponseEntity<StudyGroupEntity> createStudyGroup(@Valid @RequestBody CreateGroupDTO request) {
        Long hostUserId = 1L;

        StudyGroupEntity createdGroup = boardService.createGroup(
                hostUserId,
                request.getGroupName(),
                request.getReason(),
                request.getCapacity(),
                request.getPurpose(),
                request.getRegion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> requestJoinGroup(@PathVariable Long groupId) {
        Long guestUserId = 2L;

        try {
            boardService.requestJoinGroup(groupId, guestUserId);
            return ResponseEntity.ok("Invitation code sent to your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process join request.");
        }
    }

    // 그룹 참가 확정 엔드포인트 (초대 코드 검증)
    @PostMapping("/{groupId}/join/confirm")
    public ResponseEntity<?> confirmJoinGroup(@PathVariable Long groupId,
                                              @Valid @RequestBody JoinRequestDTO request) {
        Long guestUserId = 2L;

        try {
            StudyMembersEntity member = boardService.confirmJoinGroup(groupId, guestUserId, request.getInvitationCode());
            return ResponseEntity.ok("Successfully joined the group! Member ID: " + member.getMemberId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to confirm join request.");
        }
    }
}
