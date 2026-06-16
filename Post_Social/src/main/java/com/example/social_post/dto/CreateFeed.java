package com.example.social_post.dto;

import jakarta.validation.constraints.NotNull;

public record CreateFeed(
        @NotNull
        String userId,
        @NotNull
        String postId
) {
}
