package com.example.meetty.image.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.image.entity.UserImageEntity;
import com.example.meetty.image.repository.UserImageRepository;
import com.example.meetty.image.uploader.GcpImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;
    private final GcpImageUploader gcpImageUploader;

    // 프로필 이미지 업로드 (파일 또는 기본 이미지)
    public String uploadUserImage(UserEntity userEntity, MultipartFile image, boolean isDefaultImage) {
        try {
            // 🔍 이미지 없는 경우 (변경 없음)
            if (!isDefaultImage && (image == null || image.isEmpty())) {
                log.info("✅ 이미지 변경 없음 → 기존 이미지 유지");

                if (userEntity.getUserImageEntity() != null) {
                    return userEntity.getUserImageEntity().getUrl();
                } else {
                    log.warn("⚠️ 기존 이미지 없음 → 기본 이미지 사용");
                    return gcpImageUploader.getDefaultImageUrl();
                }
            }

            // 기존 이미지 삭제
            userImageRepository.deleteByUserEntity(userEntity);
            log.info("🗑️ 기존 이미지 삭제 완료");

//            // GCP에 새 이미지 업로드
//            String imageUrl = isDefaultImage
//                    ? DEFAULT_IMAGE_URL
//                    : gcpImageUploader.upload(image);

            // 이미지 업로드
            String imageUrl;
            if (isDefaultImage) {
                imageUrl = gcpImageUploader.getDefaultImageUrl();
                log.info("✅ 기본 이미지로 변경");
            } else {
                log.info("📤 GCP 이미지 업로드 시작 - 파일명: {}, 크기: {}, 타입: {}",
                        image.getOriginalFilename(),
                        image.getSize(),
                        image.getContentType());
                imageUrl = gcpImageUploader.upload(image);
            }

            // 새 이미지 정보 저장
            UserImageEntity userImageEntity = new UserImageEntity(userEntity, imageUrl);
            userImageRepository.save(userImageEntity);
            log.info("✅ 새 이미지 정보 DB 저장 완료: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("❌ 이미지 업로드 중 예외 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }


    // 외부 이미지 URL을 GCP에 업로드
    public String uploadUserImageFromUrl(UserEntity userEntity, String imageUrl) {
        try {
            userImageRepository.deleteByUserEntity(userEntity);

            String uploadedUrl = gcpImageUploader.uploadFromUrl(imageUrl);

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, uploadedUrl);
            userImageRepository.save(userImageEntity);

            return uploadedUrl;

        } catch (Exception e) {
            log.error("외부 이미지 저장 중 오류 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "외부 이미지 업로드에 실패했습니다.");
        }
    }

    // 유저의 프로필 이미지 삭제
    public void deleteByUser(UserEntity userEntity) {
        userImageRepository.deleteByUserEntity(userEntity);
        log.info("🗑️ 유저({})의 프로필 이미지 삭제 완료", userEntity.getEmail());
    }
}
