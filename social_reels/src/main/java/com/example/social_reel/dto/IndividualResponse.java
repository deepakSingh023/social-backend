package com.example.social_reel.dto;

public record IndividualResponse(
        PersonalReelsResponse reel,
        boolean isOwner
) {
}
