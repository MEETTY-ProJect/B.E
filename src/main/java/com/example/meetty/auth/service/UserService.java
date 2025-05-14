package com.example.meetty.auth.service;

import com.example.meetty.auth.dto.LoginRequestDto;
import com.example.meetty.auth.dto.LoginResponseDto;
import com.example.meetty.auth.dto.RefreshTokenResponseDto;
import com.example.meetty.auth.dto.SignUpDto;
import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.entity.UserRole;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.jwt.JwtTokenProvider;
import com.example.meetty.global.mail.service.EmailService;
import com.example.meetty.global.util.PasswordUtil;
import com.example.meetty.image.repository.UserImageRepository;
import com.example.meetty.image.service.UserImageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private final UserImageService userImageService;
    private final UserImageRepository userImageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;
    @Value("${spring.jwt.secure-cookie}")
    private boolean secureCookie;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void signUp(SignUpDto signUpDto, MultipartFile profileImage) {
        if (userRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EMAIL_DUPLICATED);
        }

        if (userRepository.findByUsername(signUpDto.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_DUPLICATED);
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

        UserEntity savedUser = userRepository.save(userEntity);

        // 이미지 업로드 (내부에서 null, 비어 있음 처리 + 기본 이미지 경로 저장)
        userImageService.uploadUserImage(savedUser, profileImage, false);

        // 이메일로 인증링크 발송
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("emailToken:" + token, savedUser.getEmail(), Duration.ofMinutes(60));

        try {
            emailService.sendVerificationLink(savedUser.getEmail(), token);
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패 - 회원가입 롤백: {}", savedUser.getEmail());
            throw new AppException(ErrorCode.EMAIL_SEND_FAIL);
        }

        log.info("✅ 회원가입 완료 - 이메일: {}, 인증 링크 발송됨", savedUser.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND)
        );

        if (!userEntity.isVerified() && userEntity.getProvider() == null) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if(!Objects.equals(passwordUtil.encrypt(loginRequestDto.getPassword()), userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        String email = userEntity.getEmail();
        String role = userEntity.getRole().getType();
        Long userId = userEntity.getUserId();

        String accessToken = jwtTokenProvider.createAccessToken(email, userId,role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, userId, role);

        refreshTokenRedisService.saveRefreshToken(userId, refreshToken, Duration.ofMillis(refreshExpirationTime));

        httpServletResponse.addHeader("Authorization", "Bearer " + accessToken);
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");

        httpServletResponse.addCookie(refreshCookie);

        log.info("✅ 로그인 성공 - 이메일: {}", userEntity.getEmail());
        log.info("🔑 AccessToken: {}", accessToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .address(userEntity.getAddress())
                .profileImage(userEntity.getUserImageEntity().getUrl())
                .role(userEntity.getRole().getType())
                .build();
    }

    public RefreshTokenResponseDto refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String refreshToken = getRefreshTokenFromCookie(httpServletRequest);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtTokenProvider.getEmailByToken(refreshToken);
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND)
        );
        Long userId = userEntity.getUserId();

        String savedRefreshToken = refreshTokenRedisService.getRefreshTokenByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND_REFRESH_TOKEN)
        );

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new AppException(ErrorCode.INCORRECT_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email, userId, userEntity.getRole().getType());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email, userId, userEntity.getRole().getType());

        setRefreshTokenCookie(httpServletResponse, newRefreshToken);

        // ✅ 이메일 → userId 기준으로 저장
        refreshTokenRedisService.saveRefreshToken(userEntity.getUserId(), newRefreshToken, Duration.ofMillis(refreshExpirationTime));

        log.info("✅ 토큰 재발급 성공 - 이메일: {}", email);
        log.info("🔑 AccessToken: {}", newAccessToken);

        return new RefreshTokenResponseDto(newAccessToken, "토큰이 갱신되었습니다.");
    }

    // Cookie에서 refresh_token 가져오기
    public String getRefreshTokenFromCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();

        if (cookies == null || cookies.length == 0) {
            throw new AppException(ErrorCode.NOT_FOUND_COOKIE);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseThrow(
                        () -> new AppException(ErrorCode.NOT_FOUND_REFRESH_TOKEN)
                );
    }

    // Cookie에 새로운 refresh_token 발급
    public void setRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken) {
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(secureCookie);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int)refreshExpirationTime / 1000);

        httpServletResponse.addCookie(refreshCookie);
    }

    public void logout(Long userId, HttpServletResponse response) {
        refreshTokenRedisService.deleteToken(userId);

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }

    // TODO: 회원탈퇴 시 관련된 거 전부 제거하기 cascade = CascadeType.REMOVE, orphanRemoval = true 설정
    @Transactional
    public void withdrawalUser(String loginEmail, String requestBodyPassword, HttpSession httpSession, HttpServletResponse httpServletResponse) {
        UserEntity userEntity = userRepository.findByEmail(loginEmail).orElseThrow(
                () -> new AppException(ErrorCode.USER_EMAIL_NOT_FOUND)
        );

        if (!Objects.equals(passwordUtil.encrypt(requestBodyPassword), userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        logout(userEntity.getUserId(), httpServletResponse);
        userImageRepository.deleteByUserEntity(userEntity);
        userRepository.delete(userEntity);

        entityManager.flush();
        entityManager.clear();
        log.info("Hibernate 캐시 정리 완료");

        httpSession.invalidate();
        SecurityContextHolder.clearContext();

        log.info("회원탈퇴 완료 - ID: {}", userEntity.getUserId());
    }
}
