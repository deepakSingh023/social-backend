package com.example.auth_social.dto;

public record CreateProfile(
        String username,
        String email,
        String userId
) {
}
