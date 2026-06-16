package com.example.social_post.dto;

import com.example.social_post.ImpressionType;

public record IncrementDecDto(
        String postId,
        ImpressionType type,
        int num
) {
}
