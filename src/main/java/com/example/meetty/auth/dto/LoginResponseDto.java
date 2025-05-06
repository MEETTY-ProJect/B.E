package com.example.meetty.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String accessToken;
    private String email;
    private String username;
    private String address;
    private String profileImage;
    private String role;
}
