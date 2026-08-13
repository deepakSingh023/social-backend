package com.example.Friend_Feed.dto;

public record InteractionFeedEvent(
        String eventId,
        String userId,
        String otherUserId
) {}