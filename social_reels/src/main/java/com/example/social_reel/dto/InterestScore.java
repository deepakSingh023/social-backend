package com.example.social_reel.dto;

import java.time.Instant;

public record InterestScore(
        double score,       // 0.0 → 1.0 (or slightly above)
        Instant lastUpdated
) {
}
