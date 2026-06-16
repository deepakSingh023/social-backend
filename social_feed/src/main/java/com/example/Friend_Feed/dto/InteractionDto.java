package com.example.Friend_Feed.dto;

import jakarta.validation.constraints.NotNull;

public record InteractionDto(

        @NotNull
        String authorId,

        @NotNull
        String feedOwnerId
) {
}
