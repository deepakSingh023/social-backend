package com.example.Friend_Feed.utils;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "likes" , url = "${likes.uri}")
public interface LikesClient {

    @PostMapping("/api/likes/isLiked")
    Map<String,Boolean> getLiked(
            @RequestParam String userId,
            @RequestBody List<String> postIds,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
