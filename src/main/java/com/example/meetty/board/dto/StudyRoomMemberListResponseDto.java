package com.example.meetty.board.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class StudyRoomMemberListResponseDto {
    private Long userId;
    private String userName;
    private String profileImage;


    @QueryProjection
    public StudyRoomMemberListResponseDto(Long userId, String userName, String profileImage) {
        this.userId = userId;
        this.userName = userName;
        this.profileImage = profileImage;
    }

}
