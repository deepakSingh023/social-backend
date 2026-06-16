package com.example.social_interest.dto;

import com.example.social_interest.entity.InterestScore;

import java.time.Instant;
import java.util.Map;

public record InterestDto(
        String id,
        String userId,
        Map<String, InterestScore> interests,
        Instant lastUpdated
) {
}
