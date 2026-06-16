package com.example.social_reel.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record UserInterest(
        String id,

        String userId,

        Map<String, InterestScore>interests,

        Instant lastUpdated
) {
}
