package com.example.meetty.auth.controller;

import com.example.meetty.auth.dto.LoginRequestDto;
import com.example.meetty.auth.dto.LoginResponseDto;
import com.example.meetty.auth.dto.RefreshTokenResponseDto;
import com.example.meetty.auth.dto.SignUpDto;
import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.auth.service.UserService;
import com.example.meetty.global.config.auth.CustomUserDetails;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.validation.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "회원 관련 API", description = "회원가입, 로그인, 회원탈퇴 같이 회원 데이터와 관련된 API")
public class AuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final UserRepository userRepository;

    @Operation(summary = "회원가입", description = "JSON 데이터(signUpDto)와 이미지 파일(image)을 함께 업로드하는 회원가입 API")
    @PostMapping(value = "/signUp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> signUp(
            @Parameter(
                    description = "회원가입 정보(JSON)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpDto.class)
                    )
            )
            @RequestPart(value = "signUpDto") String dtoJson,
            @Parameter(
                    description = "회원 프로필 이미지 파일",
                    content = @Content(
                            mediaType = "profileImage/png",
                            schema = @Schema(type = "String", format = "binary")
                    )
            )
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws Exception {

        log.info("[POST] 회원가입 요청");

        SignUpDto signUpDto = objectMapper.readValue(dtoJson, SignUpDto.class);
        validationService.validate(signUpDto);

        userService.signUp(signUpDto, profileImage);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다. 이메일 인증을 완료해주세요."));
    }

    @Operation(summary = "로그인", description = "이메일과 패스워드로 로그인하는 API")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse httpServletResponse) {

        log.info("[POST] 로그인 요청");

        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage())
        );

        if (!userEntity.isVerified() && userEntity.getProvider() == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED, ErrorCode.EMAIL_NOT_VERIFIED.getMessage());
        }

        try {
            LoginResponseDto loginResponseDto = userService.login(loginRequestDto, httpServletResponse);
            return ResponseEntity.ok(ApiResponse.success(loginResponseDto));

        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ApiResponse.fail(e.getErrorCode()));
        }
    }

    @Operation(summary = "토큰 재발급", description = "쿠키에 저장된 refresh_token을 활용하여 토큰을 재발급하는 API")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> refreshToken(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        log.info("[POST] 토큰 재발급 요청");

        try {
            RefreshTokenResponseDto refreshTokenResponseDto = userService.refreshToken(httpServletRequest, httpServletResponse);
            return ResponseEntity.ok(ApiResponse.success(refreshTokenResponseDto));
        } catch (AppException e) {
            log.warn("❗토큰 재발급 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ApiResponse.fail(e.getErrorCode()));
        }
    }

    @Operation(summary = "로그아웃", description = "로그아웃 API.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            HttpServletResponse response) {

        String email = customUserDetails.getUsername();
        userService.logout(email, response);

        log.info("[POST] 로그아웃: {}", email);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }
}
