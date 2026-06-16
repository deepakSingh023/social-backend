package com.example.Friend_Feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeed(
        @NotNull
        String userId,
        @NotNull
        String postId
) {
}
