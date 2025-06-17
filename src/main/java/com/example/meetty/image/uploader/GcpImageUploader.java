package com.example.meetty.image.uploader;

import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcpImageUploader {

    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    private static final String DEFAULT_IMAGE_FILENAME = "default.png";

    // ✅ 업로드: MultipartFile
    public String upload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "업로드할 이미지가 비어 있습니다.");
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            log.info("GCP 업로드 시작 - 파일명: {}, Content-Type: {}", fileName, contentType);

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, file.getBytes());

            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            log.error("❌ GCP 업로드 실패: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "GCP 업로드 중 오류 발생");
        }
    }

    // ✅ 업로드: 외부 URL
    public String uploadFromUrl(String imageUrl) {
        try {
            String fileName = UUID.randomUUID() + ".jpg";
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            try (InputStream in = new URL(imageUrl).openStream()) {
                storage.create(blobInfo, in);
            }

            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            log.error("❌ 외부 URL 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "외부 이미지 업로드 실패");
        }
    }

    // ✅ 삭제
    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("/")) return;
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (fileName.isBlank() || fileName.equals(DEFAULT_IMAGE_FILENAME)) return;

        BlobId blobId = BlobId.of(bucketName, fileName);
        boolean deleted = storage.delete(blobId);

        if (deleted) {
            log.info("🗑️ GCP 이미지 삭제 완료: {}", fileName);
        } else {
            log.warn("⚠️ GCP 이미지 삭제 실패 (존재하지 않음): {}", fileName);
        }
    }

    // ✅ 기본 이미지 URL 반환
    public String getDefaultImageUrl() {
        return "https://storage.googleapis.com/" + bucketName + "/" + DEFAULT_IMAGE_FILENAME;
    }
}

