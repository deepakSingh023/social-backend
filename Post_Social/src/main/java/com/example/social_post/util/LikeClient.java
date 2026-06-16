package com.example.social_post.util;

import com.example.social_post.dto.PostLiked;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "like-service" , url="${like.url}")
public interface LikeClient {

    @PostMapping("/api/likes/isLiked")
    Map<String, Boolean> getLikedStatus(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestParam String userId,
            @RequestBody List<String> postIds
    );


    @GetMapping("/api/likes/isLikedIndividual")
    boolean getIndividualLiked(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestParam String userId,
            @RequestParam String postId
    );


}