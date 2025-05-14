package com.example.meetty.myPage.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.util.PasswordUtil;
import com.example.meetty.image.service.UserImageService;
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

    public MyPageResponseDto getMyPage(Long userId) {
        UserEntity userEntity = userRepository.findByUserId(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        String profileImageUrl = userEntity.getUserImageEntity().getUrl();

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
                        () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        log.info("수정 전 username: {}", userEntity.getUsername());
        log.info("수정 전 address: {}", userEntity.getAddress());
        log.info("받은 dto.username: {}", updateUserDto.getUsername());
        log.info("받은 dto.address: {}", updateUserDto.getAddress());

        if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().isBlank()) {
            if (!updateUserDto.getUsername().equals(userEntity.getUsername())
                    && userRepository.findByUsername(updateUserDto.getUsername()).isPresent()) {
                throw new AppException(ErrorCode.USERNAME_DUPLICATED);
            }
            userEntity.setUsername(updateUserDto.getUsername());
        }

        // 주소 변경: 값이 있을 때만
        if (updateUserDto.getAddress() != null && !updateUserDto.getAddress().isBlank()) {
            userEntity.setAddress(updateUserDto.getAddress());
        }

        // 프로필 이미지 변경 처리
        if (updateUserDto.isResetImage()) {
            userImageService.uploadUserImage(userEntity, null, true);
        } else if (profileImage != null && !profileImage.isEmpty()) {
            userImageService.uploadUserImage(userEntity, profileImage, false);
        }

        log.info("요청 DTO - username: {}, address: {}, resetImage: {}",
                updateUserDto.getUsername(),
                updateUserDto.getAddress(),
                updateUserDto.isResetImage());

        log.info("수정 후 username: {}", userEntity.getUsername());
        log.info("수정 후 address: {}", userEntity.getAddress());

        // 응답용 이미지 URL
        String imageUrl = userEntity.getUserImageEntity().getUrl();

        return MyPageResponseDto.builder()
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .address(userEntity.getAddress())
                .profileImage(imageUrl)
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
