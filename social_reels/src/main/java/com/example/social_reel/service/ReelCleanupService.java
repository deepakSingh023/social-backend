package com.example.social_reel.service;

import com.example.social_reel.dto.ReelUpdate;
import com.example.social_reel.entity.Reel;
import com.example.social_reel.util.ProfileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ReelCleanupService {

    private final S3Client s3Client;
    private final ProfileClient profileClient;
    private final ReelLikesCleanupService reelLikesCleanupService;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    @Value("${service.secret}")
    private String token;

    @Async("cleanupExecutor")
    public void cleanupReel(
            Reel reel,
            String userId
    ) {

        try {

            String key = reel.getVideoUrl()
                    .replace(publicBaseUrl + "/", "");

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            profileClient.updateReelCounter(
                    token,
                    new ReelUpdate(userId, -1)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        reelLikesCleanupService.cleanup(
                reel.getId()
        );
    }
}