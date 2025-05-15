package com.example.meetty.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignUpDto {
    @NotBlank(message = "이메일을 입력하세요.")
    @Pattern(regexp = "^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력하세요.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,8}$", message = "닉네임은 특수문자를 제외한 2~8글자여야 합니다.")
    private String username;

    @NotBlank(message = "주소를 입력하세요.")
    private String address;
}
