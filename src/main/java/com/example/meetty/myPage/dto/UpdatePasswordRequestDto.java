package com.example.meetty.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordRequestDto {
    @Schema(description = "현재 비밀번호", example = "oldPassword123!")
    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "newPassword123!")
    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String newPassword;

    @Schema(description = "새 비밀번호 확인", example = "newPassword123!")
    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String confirmPassword;
}
