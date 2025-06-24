package com.example.meetty.myPage.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.util.PasswordUtil;
import com.example.meetty.image.entity.UserImageEntity;
import com.example.meetty.image.repository.UserImageRepository;
import com.example.meetty.image.service.UserImageService;
import com.example.meetty.image.uploader.GcpImageUploader;
import com.example.meetty.myPage.dto.MyPageResponseDto;
import com.example.meetty.myPage.dto.UpdatePasswordRequestDto;
import com.example.meetty.myPage.dto.UpdateUserInfoRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final UserImageService userImageService;
    private final PasswordUtil passwordUtil;
    private final UserImageRepository userImageRepository;
    private final GcpImageUploader gcpImageUploader;

    public MyPageResponseDto getMyPage(Long userId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage())
        );

        UserImageEntity image = userImageRepository.findByUserEntity(userEntity);
        String profileImageUrl = image != null ? image.getUrl() : gcpImageUploader.getDefaultImageUrl();

        return MyPageResponseDto.builder()
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .address(userEntity.getAddress())
                .profileImage(profileImageUrl)
                .build();
    }

    @Transactional
    public MyPageResponseDto updateUserInfo(Long userId, UpdateUserInfoRequestDto updateUserDto, MultipartFile profileImage) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage())
        );

        log.info("🔧 유저 정보 수정 시작 - userId: {}", userId);

        // 닉네임 변경
        if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().isBlank()) {
            if (!updateUserDto.getUsername().equals(userEntity.getUsername())
                    && userRepository.findByUsername(updateUserDto.getUsername()).isPresent()) {
                throw new AppException(ErrorCode.USERNAME_DUPLICATED, "이미 사용 중인 닉네임입니다.");
            }
            log.info("✏️ 닉네임 변경: {} → {}", userEntity.getUsername(), updateUserDto.getUsername());
            userEntity.setUsername(updateUserDto.getUsername());
        }

        // 주소 변경
        if (updateUserDto.getAddress() != null && !updateUserDto.getAddress().isBlank()) {
            log.info("🏠 주소 변경: {} → {}", userEntity.getAddress(), updateUserDto.getAddress());
            userEntity.setAddress(updateUserDto.getAddress());
        }

        // 프로필 이미지 처리
        String newImageUrl = null;

        if (updateUserDto.isResetImage()) {
            log.info("🖼️ 프로필 이미지 기본값으로 초기화 요청");
            newImageUrl = userImageService.uploadUserImage(userEntity, null, true);
        } else if (profileImage != null && !profileImage.isEmpty()) {
            log.info("📤 새 프로필 이미지 업로드 요청");
            newImageUrl = userImageService.uploadUserImage(userEntity, profileImage, false);
        } else {
            log.info("✅ 프로필 이미지 변경 없음 → 기존 유지");
        }

        // 기존 이미지 조회 방식 변경
        if (newImageUrl == null) {
            UserImageEntity image = userImageRepository.findByUserEntity(userEntity);
            newImageUrl = image != null ? image.getUrl() : gcpImageUploader.getDefaultImageUrl();
        }

        return MyPageResponseDto.builder()
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .address(userEntity.getAddress())
                .profileImage(newImageUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public void verifyPassword(Long userId, String currentPassword) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String encryptedInput = passwordUtil.encrypt(currentPassword);

        if (!encryptedInput.equals(userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequestDto dto) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        // 현재 비밀번호 검증
        String encryptedCurrent = passwordUtil.encrypt(dto.getCurrentPassword());
        if (!encryptedCurrent.equals(userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 일치 확인
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new AppException(ErrorCode.NOT_EQUAL_PASSWORD);
        }

        // 새 비밀번호 암호화 후 저장
        String encryptedNew = passwordUtil.encrypt(dto.getNewPassword());
        userEntity.setPassword(encryptedNew);

        log.info("사용자 {} 비밀번호 변경 완료", userId);
    }
}
