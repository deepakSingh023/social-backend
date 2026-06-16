package com.example.social_reel.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record PersonalReelsResponse(
        String id,
        String username,
        String avatar,
        String videoUrl,
        List<String> rawTags,
        Set<String> semanticTags,
        String caption,
        long viewCount,
        long likes,
        long comments,
        Instant createdAt,
        boolean isLiked
) {
}
