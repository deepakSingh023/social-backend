package com.example.social_interaction.dto;


public record InteractionFeedEvent(
        String eventId,
        String authorId,
        String feedOwnerId
) {}
