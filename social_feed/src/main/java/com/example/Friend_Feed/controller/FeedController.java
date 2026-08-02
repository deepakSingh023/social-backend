package com.example.Friend_Feed.controller;
import com.example.Friend_Feed.dto.FeedResponse;
import com.example.Friend_Feed.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String cursorId
    ) {

        System.out.println("CONTROLLER ENTERED");


        if(userId == null){
            throw new RuntimeException("AUTH IS NULL");
        }
        log.info("controller reached");

        FeedResponse posts =
                postService.getFeeds(userId, cursor, cursorId);

        return ResponseEntity.ok(posts);
    }
}
