package com.example.Friend_Feed.dto;

public record PostFeedEvent(
        String eventId,
        String postId,
        String authorId
) {}