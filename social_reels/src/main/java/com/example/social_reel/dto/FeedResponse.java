package com.example.social_reel.dto;

import java.util.List;

public record FeedResponse(

        List<ReelResponse> reels,

        String nextCursor

) {}