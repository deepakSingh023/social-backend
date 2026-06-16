package com.example.social_view.dto;

import com.example.social_view.enums.InterestType;

public record ViewDto(
        String reelId,
        InterestType type
) {
}
