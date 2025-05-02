package com.example.meetty.auth.service;

import com.example.meetty.auth.dto.SignUpDto;
import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.entity.UserRole;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.util.PasswordUtil;
import com.example.meetty.image.service.UserImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private final UserImageService userImageService;

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
                .createdAt(LocalDateTime.now())
                .build();

        boolean isDefaultImage = false;
        if (profileImage == null || profileImage.isEmpty()) {
            profileImage = userImageService.getDefaultProfileImage();
            isDefaultImage = true;
        }

        UserEntity savedUser = userRepository.save(userEntity);

        userImageService.uploadUserImage(savedUser, profileImage, isDefaultImage);

        log.info("✅ 회원가입 완료 - 이메일: {}", savedUser.getEmail());
    }
}
