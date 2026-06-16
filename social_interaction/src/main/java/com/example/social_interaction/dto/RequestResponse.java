package com.example.social_interaction.dto;

import com.example.social_interaction.entity.FriendRequest;

import java.util.List;

public record RequestResponse(
        List<FriendRequest> requestList,
        String cursor
) {
}
