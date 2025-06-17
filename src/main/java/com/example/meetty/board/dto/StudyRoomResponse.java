package com.example.meetty.board.dto;

import com.example.meetty.board.entity.StudyPurpose;
import com.example.meetty.board.entity.StudyRoomEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudyRoomResponse {
    private Long id;
    private String roomName;
    private String hostName;
    private String introduction;
    private int capacity;
    private int currentMemberCount;
    private StudyPurpose purpose;
    private String region;
    private String imageUrl;

    public StudyRoomResponse(StudyRoomEntity studyGroup, int currentMemberCount, String imageUrl) {
        this.id = studyGroup.getRoomId();
        this.roomName = studyGroup.getRoomName();
        this.hostName = studyGroup.getHostName();
        this.introduction = studyGroup.getIntroduction();
        this.capacity = studyGroup.getCapacity();
        this.purpose = studyGroup.getPurpose();
        this.region = studyGroup.getRegion();
        this.imageUrl = imageUrl;
        this.currentMemberCount = currentMemberCount;
    }
}
