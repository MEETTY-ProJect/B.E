package com.example.meetty.board.repository;

import com.example.meetty.board.entity.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity,Long> {
}
