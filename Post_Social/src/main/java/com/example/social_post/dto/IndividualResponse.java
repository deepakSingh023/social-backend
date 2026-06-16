package com.example.social_post.dto;

public record IndividualResponse(
        PostResponseDto data,
        boolean isOwner
) {
}
