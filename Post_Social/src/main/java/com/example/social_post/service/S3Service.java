package com.example.social_post.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.posts-public-url}")
    private String publicBaseUrl;

    @Async
    public CompletableFuture<String> uploadBytesAsync(byte[] bytes, String originalName, String contentType) {
        String url = uploadBytes(bytes, originalName, contentType);
        return CompletableFuture.completedFuture(url);
    }

    public String uploadBytes(byte[] bytes, String originalName, String contentType) {
        String key = UUID.randomUUID() + "-" + originalName;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(bytes));
        return publicBaseUrl + "/" + key;
    }

    public void deleteFile(String fileUrl) {
        String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(req);
    }

    @Async
    public CompletableFuture<Void> deleteFileAsync(String fileUrl) {
        deleteFile(fileUrl);
        return CompletableFuture.completedFuture(null);
    }
}
