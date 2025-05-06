package com.example.meetty.auth.service;

import com.example.meetty.auth.dto.LoginRequestDto;
import com.example.meetty.auth.dto.LoginResponseDto;
import com.example.meetty.auth.dto.SignUpDto;
import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.entity.UserRole;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.jwt.JwtTokenProvider;
import com.example.meetty.global.mail.service.EmailService;
import com.example.meetty.global.util.PasswordUtil;
import com.example.meetty.image.service.UserImageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private final UserImageService userImageService;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    public void signUp(SignUpDto signUpDto, MultipartFile profileImage) throws Exception {
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EMAIL_DUPLICATED, ErrorCode.USER_EMAIL_DUPLICATED.getMessage());
        }

        if (userRepository.findByUsername(signUpDto.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED, ErrorCode.USERNAME_DUPLICATED.getMessage());
        }

        UserEntity userEntity = UserEntity.builder()
                .email(signUpDto.getEmail())
                .password(passwordUtil.encrypt(signUpDto.getPassword()))
                .username(signUpDto.getUsername())
                .address(signUpDto.getAddress())
                .role(UserRole.ROLE_USER)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        boolean isDefaultImage = false;
        if (profileImage == null || profileImage.isEmpty()) {
            profileImage = userImageService.getDefaultProfileImage();
            isDefaultImage = true;
        }

        UserEntity savedUser = userRepository.save(userEntity);
        userImageService.uploadUserImage(savedUser, profileImage, isDefaultImage);

        // 이메일로 인증링크 발송
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("emailToken:" + token, savedUser.getEmail(), Duration.ofMinutes(60));

        try {
            emailService.sendVerificationLink(savedUser.getEmail(), token);
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패 - 회원가입 롤백: {}", savedUser.getEmail());
            throw new AppException(ErrorCode.EMAIL_SEND_FAIL, ErrorCode.EMAIL_SEND_FAIL.getMessage());
        }

        log.info("✅ 회원가입 완료 - 이메일: {}, 인증 링크 발송됨", savedUser.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND, ErrorCode.USER_EMAIL_NOT_FOUND.getMessage())
        );

        if(!Objects.equals(passwordUtil.encrypt(loginRequestDto.getPassword()), userEntity.getPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD, ErrorCode.NOT_EQUAL_PASSWORD.getMessage());
        }

        String accessToken = jwtTokenProvider.createAccessToken(userEntity.getEmail(), userEntity.getRole().getType());
        String refreshToken = jwtTokenProvider.createRefreshToken(userEntity.getEmail(), userEntity.getRole().getType());

        httpServletResponse.addHeader("Authorization", "Bearer " + accessToken);
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");

        httpServletResponse.addCookie(refreshCookie);

        log.info("✅ 로그인 성공 - 이메일: {}", userEntity.getEmail());
        log.info("🔑 AccessToken: {}", accessToken);
        log.info("🔄 RefreshToken: {}", refreshToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .address(userEntity.getAddress())
                .profileImage(userEntity.getUserImageEntity().getUrl())
                .role(userEntity.getRole().getType())
                .build();
    }
}
