package com.example.social_interaction.dto;

import com.example.social_interaction.entity.Friends;

import java.util.List;

public record SearchRequest(
        List<Response> friends,
        String cursor,
        boolean hasMore
) {
}
