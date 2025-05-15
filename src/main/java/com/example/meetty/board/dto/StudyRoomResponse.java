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
    private String introduction;
    private int capacity;
    private int currentMemberCount;
    private StudyPurpose purpose;
    private String region;
    private String hostNickname;

    public StudyRoomResponse(StudyRoomEntity studyGroup, int currentMemberCount) {
        this.id = studyGroup.getRoomId();
        this.roomName = studyGroup.getRoomName();
        this.introduction = studyGroup.getIntroduction();
        this.capacity = studyGroup.getCapacity();
        this.purpose = studyGroup.getPurpose();
        this.region = studyGroup.getRegion();
        this.hostNickname = studyGroup.getHost().getUsername();
        this.currentMemberCount = currentMemberCount;
    }
}
