package com.example.meetty.board.dto;

import com.example.meetty.board.entity.StudyRoomEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MyStudyRoomListCardResponse {
    private Long id;
    private String roomName;
    private String roomImage;
    private String hostCheck;

    public MyStudyRoomListCardResponse(StudyRoomEntity studyGroup, Long currentUserId) {
        this.id = studyGroup.getRoomId();
        this.roomName = studyGroup.getRoomName();
        this.roomImage = studyGroup.getImageUrl();
        this.hostCheck = (studyGroup.getHost().getUserId().equals(currentUserId)) ? "host" : "guest";
    }

}
