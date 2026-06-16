package com.example.social_fetchReels.controller;

import com.example.social_fetchReels.dto.FeedResponse;
import com.example.social_fetchReels.service.ReelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reels")
@RequiredArgsConstructor
public class ReelController {

    private final ReelService reelService;

    @GetMapping("/feed")
    public FeedResponse getFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        String userId = authentication.getName();
        int safeLimit = limit > 0 ? limit : 10;

        return reelService.getFeed(userId, cursor, safeLimit);
    }
}