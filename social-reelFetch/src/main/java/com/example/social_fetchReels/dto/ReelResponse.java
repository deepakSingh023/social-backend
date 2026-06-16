package com.example.social_fetchReels.dto;

import java.time.Instant;
import java.util.List;

public record ReelResponse(
        String id,
        String username,
        String avatar,
        String videoUrl,
        List<String>rawTags,
        long viewCount,
        Instant createdAt,
        String userId,
        boolean isLiked
) {
}
