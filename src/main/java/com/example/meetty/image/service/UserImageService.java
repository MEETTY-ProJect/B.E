package com.example.meetty.image.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.image.entity.UserImageEntity;
import com.example.meetty.image.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/profiles/";
    private static final String DB_PATH_PREFIX = "/uploads/profiles/";
    private static final String DEFAULT_IMAGE_PATH = DB_PATH_PREFIX + "default.png";

    // 프로필 이미지 업로드 (MultipartFile 또는 기본 이미지 경로 자동 처리)
    public String uploadUserImage(UserEntity userEntity, MultipartFile image, boolean isDefaultImage) {
        try {
            if (!isDefaultImage && (image == null || image.isEmpty())) {
                log.info("이미지 변경 없음 → 기존 이미지 유지");
                return userEntity.getUserImageEntity().getUrl();
            }

            userImageRepository.deleteByUserEntity(userEntity);

            String dbFilePath = isDefaultImage ? DEFAULT_IMAGE_PATH : saveImage(image, UPLOAD_DIR);

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

            return dbFilePath;

        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, ErrorCode.IMAGE_UPLOAD_FAILED.getMessage());
        }
    }

    // 외부 URL에서 이미지 저장
    public String uploadUserImageFromUrl(UserEntity userEntity, String imageUrl) {
        try {
            userImageRepository.deleteByUserEntity(userEntity);

            String dbFilePath = saveImageFromUrl(imageUrl, UPLOAD_DIR);

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

            return dbFilePath;
        } catch (IOException e) {
            log.error("외부 이미지 저장 중 오류 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "외부 이미지 업로드에 실패했습니다.");
        }
    }

    // 기본 이미지 경로 반환
    public String getDefaultImagePath() {
        return DEFAULT_IMAGE_PATH;
    }

    // MultipartFile → 로컬 저장 후 DB 경로 반환
    public String saveImage(MultipartFile image, String uploadsDir) throws IOException {
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        String filePath = uploadsDir + fileName;
        String dbFilePath = "/uploads/profiles/" + fileName;

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());

        return dbFilePath;
    }

    // 외부 이미지 URL -> 로컬 파일 저장
    private String saveImageFromUrl(String imageUrl, String uploadsDir) throws IOException {
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        String filePath = uploadsDir + fileName;
        String dbFilePath = DB_PATH_PREFIX + fileName;

        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.createDirectories(Paths.get(uploadsDir));
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }

        return dbFilePath;
    }
}
