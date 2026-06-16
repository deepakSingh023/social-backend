package com.example.social_interest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestScore {
    private double score;        // 0.0 → 1.0 (or slightly above)
    private Instant lastUpdated; // per-tag update time
}
