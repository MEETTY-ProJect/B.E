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

    // 프로필 이미지 업로드 (MultipartFile or URL)
    public void uploadUserImage(UserEntity userEntity, MultipartFile image, boolean isDefaultImage) {
        try {
            String dbFilePath;

            if (isDefaultImage) {
                dbFilePath = "/uploads/profiles/default.png";
            } else {
                String uploadDir = "src/main/resources/static/uploads/profiles/";
                dbFilePath = saveImage(image, uploadDir);
            }

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, ErrorCode.IMAGE_UPLOAD_FAILED.getMessage());
        }
    }


    // 외부 이미지 URL 직접 저장
    public void uploadUserImageFromUrl(UserEntity userEntity, String imageUrl) {
        try {
            String uploadDir = "src/main/resources/static/uploads/profiles/";
            String dbFilePath = saveImageFromUrl(imageUrl, uploadDir);

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생", e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, ErrorCode.IMAGE_UPLOAD_FAILED.getMessage());
        }
    }

    // 기본 이미지 반환
    public MultipartFile getDefaultProfileImage() throws IOException {
        ClassPathResource defaultImage = new ClassPathResource("static/uploads/profiles/default.png");
        try (InputStream in = defaultImage.getInputStream()) {
            return new MockMultipartFile("default.png", "default.png", MediaType.IMAGE_PNG_VALUE, in);
        }
    }

    // MultipartFile 저장
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
    public String saveImageFromUrl(String imageUrl, String uploadsDir) throws IOException {
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        String filePath = uploadsDir + fileName;
        String dbFilePath = "/uploads/profiles/" + fileName;

        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
        return dbFilePath;
    }
}
