package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.StudyGroupEntity;
import com.example.meetty.board.entity.StudyMembersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyMembersRepository extends JpaRepository<StudyMembersEntity,Long> {
    boolean existsByStudyGroupAndMember(StudyGroupEntity studyGroup, UserEntity member);
}
