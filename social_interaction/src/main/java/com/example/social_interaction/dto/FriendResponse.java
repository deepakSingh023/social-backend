package com.example.social_interaction.dto;

import com.example.social_interaction.entity.Friends;

import java.util.List;

public record FriendResponse(
            List<Response> req,
            String cursor,
            boolean hasMore
) {
}
