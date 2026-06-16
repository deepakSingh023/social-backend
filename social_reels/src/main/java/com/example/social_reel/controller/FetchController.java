package com.example.social_reel.controller;

import com.example.social_reel.dto.FetchReelDto;
import com.example.social_reel.dto.FeedResponse;
import com.example.social_reel.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reel")
public class FetchController {

    private final ViewService viewService;

    @PostMapping("/feed")
    public ResponseEntity<FeedResponse> getReels(
            @RequestBody FetchReelDto data
    ){
        return ResponseEntity.ok(
                viewService.fetchFeed(data)
        );
    }
}