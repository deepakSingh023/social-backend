package com.example.social_chat.dto;

public record ConversationDto(
        String eventId,
        String user1Id,
        String user2Id
) {
}
