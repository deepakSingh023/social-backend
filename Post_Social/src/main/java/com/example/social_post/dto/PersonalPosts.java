package com.example.social_post.dto;

import com.example.social_post.entity.Post;

import java.time.Instant;
import java.util.List;

public record PersonalPosts(
        List<PostResponseDto> posts,
        Instant cursor,
        boolean isOwner
) {
}
