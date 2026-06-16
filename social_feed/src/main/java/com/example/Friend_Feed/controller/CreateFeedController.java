package com.example.Friend_Feed.controller;


import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.InteractionDto;
import com.example.Friend_Feed.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feeds")
public class CreateFeedController {

    private final PostService postService;


    //when a user creates a new post then create feed for all the interaction
    @PostMapping("/create-feed")
    public ResponseEntity<Void> postCreateFeed(
            @RequestBody CreateFeed data
            ){

        postService.createFeed(data);

        return ResponseEntity.noContent().build();
    }




    //when a user create interaction with another user
    @PostMapping("/create-feed-iteraction")
    public ResponseEntity<Void> postCreateInteractionFeed(
            @RequestBody InteractionDto data
    ){

        postService.createInteractionFeed(data);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/delete-feed")
    public ResponseEntity<Void> deleteFeed(
            @RequestParam String feedOwnerId,
            @RequestParam String authorId
    ){

        postService.deleteFeeds(authorId,feedOwnerId);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @DeleteMapping("/delete-feed-post")
    public ResponseEntity<Void> deleteFeedPost(
            @RequestParam String postId

    ){
        postService.deleteFeedPost(postId);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }




}
