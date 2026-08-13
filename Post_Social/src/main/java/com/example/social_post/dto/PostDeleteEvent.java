package com.example.social_post.dto;

public record PostDeleteEvent(
        String eventId,
        String postId
) {
}
