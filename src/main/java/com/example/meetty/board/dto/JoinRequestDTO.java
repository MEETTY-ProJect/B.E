package com.example.meetty.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class JoinRequestDTO {
    @NotBlank(message = "초대할 회원의 닉네임을 입력해주세요.")
    private String targetUserNickname;
}
