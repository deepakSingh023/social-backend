package com.example.Friend_Feed.dto;

import java.util.List;

public record RecipientsPosts(
        List<String> postId,
        String cursor
) {
}
