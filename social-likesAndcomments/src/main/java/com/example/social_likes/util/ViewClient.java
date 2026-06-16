package com.example.social_likes.util;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "view" , url = "${view.uri}")
public interface ViewClient {

    @PostMapping("/api/view/create-like-interest")
    void createLikeInterest(@RequestParam String reelId,
                            @RequestParam String userId,
                            @RequestHeader("X-SECRET-TOKEN") String token);
}
