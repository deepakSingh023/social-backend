package com.example.social_post.util;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "interaction" , url = "${interaction.service}")
public interface InteractionClient {


    @DeleteMapping("/api/comments/denormalize/cleanup")
    void deleteLikesAndComments(
            @RequestParam String targetId,
            @RequestHeader("X-SECRET-TOKEN")String token
    );
}
