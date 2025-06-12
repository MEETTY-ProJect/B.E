package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.entity.StudyRoomEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoomEntity,Long> {
    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomName LIKE %:roomName% AND s.purpose = :purpose AND s.region = :region")
    Page<StudyRoomEntity> findByNameContainingIgnoreCaseAndPurposeAndRegionWithHost(
            @Param("roomName") String roomName, @Param("purpose") StudyPurpose purpose, @Param("region") String region, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomName LIKE %:roomName% AND s.purpose = :purpose")
    Page<StudyRoomEntity> findByNameContainingIgnoreCaseAndPurposeWithHost(
            @Param("roomName") String roomName, @Param("purpose") StudyPurpose purpose, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomName LIKE %:roomName% AND s.region = :region")
    Page<StudyRoomEntity> findByNameContainingIgnoreCaseAndRegionWithHost(
            @Param("roomName") String roomName, @Param("region") String region, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomName LIKE %:roomName%")
    Page<StudyRoomEntity> findByNameContainingIgnoreCaseWithHost(
            @Param("roomName") String roomName, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.purpose = :purpose AND s.region = :region")
    Page<StudyRoomEntity> findByPurposeAndRegionWithHost(
            @Param("purpose") StudyPurpose purpose, @Param("region") String region, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.purpose = :purpose")
    Page<StudyRoomEntity> findByPurposeWithHost(
            @Param("purpose") StudyPurpose purpose, Pageable pageable);

    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.region = :region")
    Page<StudyRoomEntity> findByRegionWithHost(
            @Param("region") String region, Pageable pageable);

    // 모든 스터디 그룹 조회 (필터링/검색 조건 없을 때) - Fetch Join 포함
    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host")
    Page<StudyRoomEntity> findAllWithHost(Pageable pageable);

    // 스터디 그룹 ID로 상세 조회 시에도 host 정보 Fetch Join
    @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomId = :id")
    Optional<StudyRoomEntity> findByIdWithHost(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("SELECT s FROM StudyRoomEntity s JOIN FETCH s.host WHERE s.roomId = :id")
     Optional<StudyRoomEntity> findByIdWithHostAndLock(@Param("id") Long id);

    void deleteByHost(UserEntity userEntity);
}
