package com.example.social_reel.dto;


import com.example.social_reel.enums.InterestType;

public record ViewDto(
        String reelId,
        InterestType type
) {
}
