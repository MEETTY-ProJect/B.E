package com.example.meetty.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PasswordRequestDto {
    @Schema(description = "비밀번호", example = "MyPass123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
