package com.example.social_post.dto;

import java.util.List;

public record RecipientsPosts(
        List<String> postId,
        String cursor
) {
}
