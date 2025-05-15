package com.example.meetty.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserInfoRequestDto {
    @Schema(description = "새 닉네임", example = "닉네임01")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,8}$", message = "닉네임은 특수문자를 제외한 2~8글자여야 합니다.")
    private String username;
    @Schema(description = "새 주소", example = "서울특별시 강남구")
    private String address;
    @Schema(description = "프로필 이미지를 기본 이미지로 재설정할지 여부", example = "false")
    private boolean resetImage;
}
