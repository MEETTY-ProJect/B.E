package com.example.meetty.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CreateGroupDTO {
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String groupName;

    @NotBlank(message = "방 개설 이유는 필수입니다.")
    private String reason;

    @NotNull(message = "인원 수는 최소 1명 입니다.")
    private Integer capacity;

    @NotBlank(message = "목적은 필수입니다.")
    private String purpose;

    private String region;
}
