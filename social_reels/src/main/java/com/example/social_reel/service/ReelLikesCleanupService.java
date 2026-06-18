package com.example.social_reel.service;

import com.example.social_reel.util.LikeClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReelLikesCleanupService {

    private final LikeClient likeClient;

    @Value("${service.secret}")
    private String token;

    @Async("cleanupExecutor")
    @Retry(name = "importantApi")
    @CircuitBreaker(
            name = "importantApi",
            fallbackMethod = "fallback"
    )
    public void cleanup(String reelId) {

        likeClient.deleteLikesAndComments(
                reelId,
                token
        );
    }

    public void fallback(
            String reelId,
            Throwable ex
    ) {

        log.error(
                "Failed to cleanup likes/comments for reel={}",
                reelId,
                ex
        );
    }
}