package com.example.social_reel.dto;

import com.example.social_reel.enums.ImpressionType;

public record IncrementDecDto(
        String postId,
        ImpressionType type,
        int num
) {
}