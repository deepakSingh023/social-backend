package com.example.social_likes.dto;

import com.example.social_likes.enums.ImpressionType;
import com.example.social_likes.enums.LikeTargetType;

public record IncrementDecDto(
        String postId,
        ImpressionType type,
        int num
) {
}
