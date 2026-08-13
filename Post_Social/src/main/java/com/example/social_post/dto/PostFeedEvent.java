package com.example.social_post.dto;

public record PostFeedEvent(
        String eventId,
        String postId,
        String authorId
) {}