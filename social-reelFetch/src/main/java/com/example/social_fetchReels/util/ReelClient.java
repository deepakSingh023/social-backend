package com.example.social_fetchReels.util;

import com.example.social_fetchReels.dto.FeedResponse;
import com.example.social_fetchReels.dto.FetchReelDto;
import com.example.social_fetchReels.dto.ReelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


@FeignClient(name="reel",url="${reel.uri}")
public interface ReelClient {


    @PostMapping("/api/reel/feed")
    FeedResponse getFeed(
            @RequestBody FetchReelDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
