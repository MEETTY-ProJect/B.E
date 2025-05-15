package com.example.meetty.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JoinRequestDTO {
    @NotBlank(message = "초대 코드는 필수입니다.")
    private String invitationCode;
}
