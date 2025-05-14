package com.example.meetty.board.dto;

import com.example.meetty.board.entity.StudyPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UpdateStudyRoomRequest {
    @NotBlank(message = "스터디 그룹 이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "스터디 그룹 소개글은 필수 입력 항목입니다.")
    private String introduction;

    @NotNull(message = "스터디 그룹 목적은 필수 입력 항목입니다.")
    private StudyPurpose purpose; // Enum 사용
}
