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
import java.util.Optional;

@Repository
public interface StudyMembersRepository extends JpaRepository<StudyMembersEntity,Long>, StudyMembersRepositoryCustom {
    // 특정 스터디 그룹의 'ACTIVE' 상태 멤버 수를 세는 메서드
    int countByStudyRoomRoomIdAndStatus(Long roomId, MemberStatus status);

    @Query("SELECT sm.studyRoom.roomId, COUNT(sm) " +
            "FROM StudyMembersEntity sm " +
            "WHERE sm.studyRoom.roomId IN :roomIds AND sm.status = :status " +
            "GROUP BY sm.studyRoom.roomId")
    List<Object[]> countActiveMembersByRoomIds(@Param("roomIds") List<Long> roomIds, @Param("status") MemberStatus status);

    //특정 스터디 그룹에 속해있는 사용자가 맞는지 확인하는 메서드
    boolean existsByStudyRoom_RoomIdAndMember_UserId(Long roomId, Long userId);

    // 특정 스터디 룸에 특정 유저의 멤버십 정보가 있는지 확인
    Optional<StudyMembersEntity> findByStudyRoomRoomIdAndMemberUserId(Long roomId, Long userId);


    // 특정 스터디 룸에 특정 유저의 특정 상태 멤버십 정보 조회 (예: PENDING 상태)
    Optional<StudyMembersEntity> findByStudyRoomRoomIdAndMemberUserIdAndStatus(Long roomId, Long userId, MemberStatus status);

    // 로그인된 회원이 속한 특정 스터디 룸의 정보 조회
    @Query("SELECT sm FROM StudyMembersEntity sm JOIN FETCH sm.studyRoom WHERE sm.member = :user AND sm.status = :status")
    List<StudyMembersEntity> findByMemberAndStatus(UserEntity user, MemberStatus status);

    //특정 스터디그룹에 속한 사용자 정보를 가져오는 메서드
    List<StudyMembersEntity> findByStudyRoom_RoomId(Long roomId);

    void deleteAllByMember(UserEntity userEntity);
}
