package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.MemberStatus;
import com.example.meetty.board.entity.StudyRoomEntity;
import com.example.meetty.board.entity.StudyMembersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMembersRepository extends JpaRepository<StudyMembersEntity,Long> {
    // 특정 스터디 그룹의 'ACTIVE' 상태 멤버 수를 세는 메서드
    int countByStudyRoomRoomIdAndStatus(Long studyGroupId, MemberStatus status);

    @Query("SELECT sm.studyRoom.roomId, COUNT(sm) " +
            "FROM StudyMembersEntity sm " +
            "WHERE sm.studyRoom.roomId IN :roomIds AND sm.status = :status " +
            "GROUP BY sm.studyRoom.roomId")
    List<Object[]> countActiveMembersByRoomIds(@Param("roomIds") List<Long> roomIds, @Param("status") MemberStatus status);

    // 특정 스터디 그룹에 특정 사용자가 'ACTIVE' 상태로 존재하는지 확인하는 메서드
    boolean existsByStudyRoomRoomIdAndMemberUserIdAndStatus(Long studyGroupId, Long userId, MemberStatus status);

    //특정 스터디 그룹에 속해있는 사용자가 맞는지 확인하는 메서드
    boolean existsByStudyRoom_RoomIdAndMember_UserId(Long roomId, Long userId);
}
