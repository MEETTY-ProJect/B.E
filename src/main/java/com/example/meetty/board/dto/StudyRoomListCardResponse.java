package com.example.meetty.board.dto;

import com.example.meetty.board.entity.StudyRoomEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StudyRoomListCardResponse {
    private Long id;
    private String roomName;
    private int currentMemberCount;
    private String hostNickname;

    public StudyRoomListCardResponse(StudyRoomEntity studyGroup, int currentMemberCount) {
        this.id = studyGroup.getRoomId();
        this.roomName = studyGroup.getRoomName();
        this.currentMemberCount = currentMemberCount;
        this.hostNickname = studyGroup.getHost().getUsername();
    }
}
