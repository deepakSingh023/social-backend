package com.example.social_fetchReels.service;

import com.example.social_fetchReels.dto.FeedResponse;
import com.example.social_fetchReels.dto.ReelResponse;
import com.example.social_fetchReels.entity.Reel;

import java.util.List;

public interface ReelService {
    FeedResponse getFeed(String userId, String cursor, int limit);
}
