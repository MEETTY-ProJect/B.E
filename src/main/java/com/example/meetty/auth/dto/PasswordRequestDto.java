package com.example.meetty.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PasswordRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
