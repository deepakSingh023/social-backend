package com.example.social_view.dto;

import com.example.social_view.enums.InterestType;

import java.util.Set;

public record InterestDto(
        String userId,
        String reelId,
        InterestType type,
        Set<String> semanticTags
) {
}
