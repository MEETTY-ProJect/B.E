package com.example.meetty.image.uploader;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcpImageUploader {

    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    public String uploadFromUrl(String imageUrl) throws IOException {
        String fileName = UUID.randomUUID() + ".jpg";
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/jpeg")
                .build();

        try (InputStream in = new URL(imageUrl).openStream()) {
            storage.create(blobInfo, in);
        }

        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("/")) return;
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (fileName.isBlank()) return;
        BlobId blobId = BlobId.of(bucketName, fileName);
        storage.delete(blobId);
    }
}

