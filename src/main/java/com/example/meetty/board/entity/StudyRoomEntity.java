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
@Table(name = "study_rooms")
public class StudyRoomEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private StudyPurpose purpose;

    @Column(name = "region")
    private String region;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id",nullable = false)
    private UserEntity host;

    public void updateInfo(String roomName, String introduction, StudyPurpose purpose) {
        this.roomName = roomName;
        this.introduction = introduction;
        this.purpose = purpose;
    }
}
