package com.example.social_chat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofSeconds(60);

    public void markOnline(String userId, String instanceId) {
        redisTemplate.opsForValue()
                .set("online:" + userId, instanceId, TTL);
    }

    public void markOffline(String userId) {
        redisTemplate.delete("online:" + userId);
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("online:" + userId)
        );
    }

    public String getUserInstance(String userId) {
        return redisTemplate.opsForValue()
                .get("online:" + userId);
    }
}
