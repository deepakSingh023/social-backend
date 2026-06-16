package com.example.social_interaction.tasks;


import com.example.social_interaction.dto.InteractionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="ineraction", url="${feed.uri}")
public interface PostClient {

    @PostMapping("/api/feeds/create-feed-iteraction")
     void createFeed(
             @RequestBody InteractionDto data,
             @RequestHeader("X-SECRET-TOKEN") String token
     );

    @DeleteMapping("/api/feeds/delete-feed")
     void deleteFeed(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestParam String feedOwnerId,
            @RequestParam String authorId
    );
}
