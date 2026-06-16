package com.example.Friend_Feed.controller;

import com.example.Friend_Feed.dto.FeedResponse;
import com.example.Friend_Feed.entity.Post;
import com.example.Friend_Feed.filter.JwtAuthenticationFilter;
import com.example.Friend_Feed.service.PostService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/getFeed")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }


    private final static Logger log = LoggerFactory.getLogger(FeedController.class);


    //api to get the feed for home page
    @GetMapping("/get")
    public ResponseEntity<FeedResponse> getFeed(
            Authentication auth,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String cursorId
    ) {

        System.out.println("CONTROLLER ENTERED");

        log.info("auth={}", auth);

        if(auth == null){
            throw new RuntimeException("AUTH IS NULL");
        }

        String userId = auth.getName();

        log.info("controller reached");

        FeedResponse posts =
                postService.getFeeds(userId, cursor, cursorId);

        return ResponseEntity.ok(posts);
    }
}
