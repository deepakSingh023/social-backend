package com.example.social_reel.dto;

public record FeedRequest(
        String cursor, // last reelId or timestamp
        int limit
) {
}
