package com.example.Friend_Feed.dto;


import com.example.Friend_Feed.entity.Post;
import java.time.Instant;
import java.util.List;

public record FeedResponse(
        List<PostResponse> posts,
        Instant nextCursor,
        String cursorId,
        boolean hasMore
) {}
