package com.example.meetty.myPage.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponseDto {
    private String email;
    private String username;
    private String address;
    private String profileImage;
}
