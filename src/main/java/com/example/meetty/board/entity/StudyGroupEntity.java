package com.example.meetty.board.entity;

import com.example.meetty.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "study_groups")
public class StudyGroupEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;
    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    @Column(name = "purpose", nullable = false)
    private String purpose;
    @Column(name = "region")
    private String region;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id",nullable = false)
    private UserEntity hostUserId;

    // StudyGroupMember 목록의 양방향 관계 (필요시 주석 해제)
    // cascade = CascadeType.ALL, orphanRemoval = true 설정 시 그룹 삭제/멤버 제거 시 연관된 멤버십 기록 자동 삭제
    // @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<StudyMembersEntity> members = new ArrayList<>();
}
