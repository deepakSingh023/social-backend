package com.example.social_post.controller;


import com.example.social_post.dto.RecipientsPosts;
import com.example.social_post.entity.Post;
import com.example.social_post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostClientController {

    private final PostService postService;


    @PostMapping("/getPosts-feed")
    public ResponseEntity<List<Post>> getFeedPosts(
            @RequestBody List<String> postIds
    ){

        List<Post> posts = postService.getPosts(postIds);
        return ResponseEntity.ok(posts);

    }

    @PostMapping("/getPost")
    public ResponseEntity<RecipientsPosts> getPosts(
            @RequestParam String authorId,
            @RequestParam int size,
            @RequestParam(required = false) String cursor
    ){

        RecipientsPosts posts = postService.getFeedPosts(authorId,cursor,size);
        return ResponseEntity.ok(posts);

    }
}
