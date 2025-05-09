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
@Table(name = "study_group_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id", "user_id"})})
public class StudyMembersEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;
    @Column(name = "joined_at",nullable = false)
    private LocalDateTime joinedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroupEntity studyGroup ;

    public enum MemberStatus {
        PENDING,
        ACTIVE
    }
    @Transient
    private String invitationCode;
}