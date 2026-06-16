package com.example.Friend_Feed.utils;


import com.example.Friend_Feed.dto.RecipientsPosts;
import com.example.Friend_Feed.entity.Feed;
import com.example.Friend_Feed.entity.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="post", url="${post.uri}")
public interface PostClient {

    @PostMapping("/api/post/getPost")
    RecipientsPosts getPosts(
            @RequestHeader("X-SECRET-TOKEN")String token,
            @RequestParam String authorId,
            @RequestParam int size,
            @RequestParam(required = false) String cursor
    );


    @PostMapping("/api/post/getPosts-feed")
    List<Post> getFeedPosts(
            @RequestHeader("X-SECRET-TOKEN")String token,
            @RequestBody List<String> postIds
    );
}
