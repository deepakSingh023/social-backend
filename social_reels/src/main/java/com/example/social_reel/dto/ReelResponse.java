package com.example.social_reel.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
