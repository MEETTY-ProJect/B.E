package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.MemberStatus;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.entity.StudyMembersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyMembersRepository extends JpaRepository<StudyMembersEntity,Long> {
    // 특정 스터디 그룹의 'ACTIVE' 상태 멤버 수를 세는 메서드
    int countByStudyRoomRoomIdAndStatus(Long studyGroupId, MemberStatus status);

    // 특정 스터디 그룹에 특정 사용자가 'ACTIVE' 상태로 존재하는지 확인하는 메서드
    boolean existsByStudyGroupIdAndMemberIdAndStatus(Long studyGroupId, Long userId, MemberStatus status);
}
