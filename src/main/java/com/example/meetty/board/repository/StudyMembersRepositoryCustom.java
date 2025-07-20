package com.example.meetty.board.repository;

import com.example.meetty.board.dto.StudyRoomMemberListResponseDto;

import java.util.List;

public interface StudyMembersRepositoryCustom {

    //특정 스터디그룹에 속한 사용자 정보를 가져오는 메서드
    List<StudyRoomMemberListResponseDto> findByStudyRoomMember_RoomId(Long roomId);
}
