package com.example.social_reel.dto;

import java.time.Instant;
import java.util.List;

public record PersonalReels(
        List<PersonalReelsResponse> reels,
        Instant cursor,
        boolean isOwner
) {
}
