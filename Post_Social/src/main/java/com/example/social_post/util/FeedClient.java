package com.example.social_post.util;

import com.example.social_post.dto.CreateFeed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "feed-service" , url="${feed.url}")
public interface FeedClient {

    @DeleteMapping("/api/feeds/delete-feed-post")
    void  deleteFeedPost(
            @RequestParam String postId,
            @RequestHeader("X-SECRET-TOKEN") String token

    );

    @PostMapping("/api/feeds/create-feed")
    void createFeed(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestBody CreateFeed data
    );

}
