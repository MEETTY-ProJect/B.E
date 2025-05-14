package com.example.meetty.myPage.controller;

import com.example.meetty.auth.dto.PasswordRequestDto;
import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.validation.ValidationService;
import com.example.meetty.myPage.dto.MyPageResponseDto;
import com.example.meetty.myPage.dto.UpdatePasswordRequestDto;
import com.example.meetty.myPage.dto.UpdateUserInfoRequestDto;
import com.example.meetty.myPage.service.MyPageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class MyPageController {

    private final UserRepository userRepository;
    private final MyPageService myPageService;
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;

    @Operation(summary = "내 정보", description = "로그인한 유저의 정보를 가져오는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/v1/me")
    ResponseEntity<ApiResponse<MyPageResponseDto>> getMyPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getUserId();
        log.info("[GET] 내 정보 요청 - userId: {}", userId);

        MyPageResponseDto myPageResponseDto = myPageService.getMyPage(userId);

        return ResponseEntity.ok(ApiResponse.success(myPageResponseDto));
    }

    @Operation(summary = "내 정보 수정", description = "닉네임, 주소, 프로필 이미지를 수정하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/v1/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MyPageResponseDto>> updateMyPage(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(
                    description = "수정할 유저 정보(JSON)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserInfoRequestDto.class)
                    )
            )
            @RequestPart(value = "updateUserDto") String dtoJson,
            @Parameter(
                    description = "새 프로필 이미지 파일",
                    content = @Content(
                            mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        Long userId = customUserDetails.getUserId();
        log.info("[PATCH] 내 정보 수정 요청 - userId: {}", userId);

        try {
            UpdateUserInfoRequestDto updateDto = objectMapper.readValue(dtoJson, UpdateUserInfoRequestDto.class);
            validationService.validate(updateDto);

            MyPageResponseDto myPageResponseDto = myPageService.updateUserInfo(userId, updateDto, profileImage);

            return ResponseEntity.ok(ApiResponse.success(myPageResponseDto));

        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INVALID_JSON_FORMAT, ErrorCode.INVALID_JSON_FORMAT.getMessage());
        }
    }

    @Operation(summary = "현재 비밀번호 확인", description = "비밀번호 변경 전에 현재 비밀번호를 검증합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/v1/password/verify")
    public ResponseEntity<ApiResponse<String>> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordRequestDto passwordRequestDto) {

        myPageService.verifyPassword(userDetails.getUserId(), passwordRequestDto.getPassword());

        return ResponseEntity.ok(ApiResponse.success("비밀번호 확인 완료"));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/v1/password/update")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequestDto passwordRequestDto) {

        myPageService.updatePassword(userDetails.getUserId(), passwordRequestDto);

        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
    }
}
