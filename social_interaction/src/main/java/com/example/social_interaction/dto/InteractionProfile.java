package com.example.social_interaction.dto;

public record InteractionProfile(
        String username,
        String userAvatar,
        String followedName,
        String followedAvatar
) {
}
