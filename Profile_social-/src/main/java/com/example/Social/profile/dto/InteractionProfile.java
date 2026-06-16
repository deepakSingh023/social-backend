package com.example.Social.profile.dto;

public record InteractionProfile(
        String username,
        String userAvatar,
        String followedName,
        String followedAvatar
) {
}
