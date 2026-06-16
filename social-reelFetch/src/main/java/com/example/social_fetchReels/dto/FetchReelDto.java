package com.example.social_fetchReels.dto;

import com.example.social_fetchReels.entity.UserInterest;

public record FetchReelDto(
        UserInterest data,
        FeedRequest req
) {
}
