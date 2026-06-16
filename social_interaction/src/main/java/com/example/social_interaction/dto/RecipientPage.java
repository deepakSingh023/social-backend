package com.example.social_interaction.dto;

import java.util.List;

public record RecipientPage(
        List<String> userIds,
        String nextCursor
) {}