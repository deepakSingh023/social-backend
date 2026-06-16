package com.example.social_post.dto;

import java.time.Instant;
import java.util.List;

public record PostResponseDto(
        String id,

        String userId,

        String avatar,

        String username,



        List<String>imageUrls,
        String videoUrl,

        String caption,

        String songUrl,
        String songName,
        String artistName,

        List<String> tags,

        boolean isPrivate,

        Instant createdAt,

        long likes ,
        long comments,

        boolean isLiked
) {
}
