package com.example.Friend_Feed.dto;

import java.util.List;

public record RecipientPage(
        List<String> userIds,
        String nextCursor
) {}