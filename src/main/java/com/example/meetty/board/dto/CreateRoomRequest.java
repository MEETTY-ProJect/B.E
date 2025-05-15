package com.example.meetty.board.dto;

import com.example.meetty.board.entity.StudyPurpose;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CreateRoomRequest {
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String roomName;

    @NotBlank(message = "스터디 그룹 소개글은 필수 입력 항목입니다.")
    private String introduction;

    @Min(value = 2, message = "스터디 그룹 인원은 최소 2명 이상이어야 합니다.")
    private Integer capacity;

    @NotNull(message = "스터디 그룹 목적은 필수 입력 항목입니다.")
    private StudyPurpose purpose;

    private String region;

}
