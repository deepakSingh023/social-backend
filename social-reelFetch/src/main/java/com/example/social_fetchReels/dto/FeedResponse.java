package com.example.social_fetchReels.dto;

import java.util.List;

public record FeedResponse(
        List<ReelResponse> reels,

        String nextCursor

) {
}
