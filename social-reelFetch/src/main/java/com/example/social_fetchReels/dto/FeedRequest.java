package com.example.social_fetchReels.dto;

import lombok.Data;

@Data
public class FeedRequest {
    private String cursor; // last reelId or timestamp
    private int limit;     // usually 5–10
}
