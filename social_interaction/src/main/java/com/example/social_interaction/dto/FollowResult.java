package com.example.social_interaction.dto;

import com.example.social_interaction.entity.Follower;

import java.util.List;

public record FollowResult(
        List<Follower> follower,
        String cursor,
        boolean hasMore
) {
}
