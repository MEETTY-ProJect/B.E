package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.JoinRequestEntity;
import com.example.meetty.board.entity.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequestEntity,Long> {
    boolean existsByStudyGroupAndUserAndExpiresAtAfter(StudyGroupEntity studyGroup, UserEntity user, java.time.LocalDateTime now);
    Optional<JoinRequestEntity> findByToken(String token);
}
