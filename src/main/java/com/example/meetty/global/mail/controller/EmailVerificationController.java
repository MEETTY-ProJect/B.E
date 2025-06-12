package com.example.meetty.global.mail.controller;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "이메일 인증 API", description = "이메일 인증 관련 API ")
public class EmailVerificationController {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    @Operation(summary = "이메일 인증", description = "회원가입 후 이메일로 받은 토큰을 검증하여 인증상태로 전환합니다.")
    @GetMapping("/verify")
    public void verifyMail(
            @Parameter(description = "이메일 인증 토큰")
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {
        String redisKey = "emailToken:" + token;
        String email = redisTemplate.opsForValue().get(redisKey);
        log.info("📦 Redis에서 가져온 이메일: {}", email);

        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json;charset=UTF-8");

        if (email == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.fail(ErrorCode.INVALID_EMAIL_TOKEN)
            ));
            return;
        }

        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND)
        );

        if (userEntity.isVerified()) {
            redisTemplate.delete(redisKey);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.fail(ErrorCode.EMAIL_ALREADY_VERIFIED)
            ));
            return;
        }

        userEntity.setVerified(true);
        userRepository.save(userEntity);
        redisTemplate.delete(redisKey);

        response.sendRedirect("/");
    }
}
