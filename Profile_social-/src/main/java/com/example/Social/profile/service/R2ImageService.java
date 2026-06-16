package com.example.Social.profile.service;

import com.example.Social.profile.utils.ImageCompressor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2ImageService {

    private final S3Client r2Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicUrl;

    /**
     * Uploads a profile pic after compressing it.
     */
    public String uploadProfilePic(MultipartFile file) {
        try {
            // Compress the image to 150px width and quality 0.6
            byte[] compressedBytes = ImageCompressor.compressProfilePic(file.getInputStream());

            String key = "profile_pics/" +
                    UUID.randomUUID() + "-" +
                    file.getOriginalFilename();

            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("image/jpeg") // compressed is JPEG
                            .build(),
                    RequestBody.fromBytes(compressedBytes)
            );

            return publicUrl + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    /**
     * Delete an image by URL
     */
    public void deleteImage(String url) {
        if (url == null || url.isEmpty()) return;

        try {
            String key = url.replace(publicUrl + "/", "");
            r2Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }
}
