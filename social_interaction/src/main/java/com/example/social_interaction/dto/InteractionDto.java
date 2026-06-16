package com.example.social_interaction.dto;

import jakarta.validation.constraints.NotNull;

public record InteractionDto(

        @NotNull
        String authorId,

        @NotNull
        String feedOwnerId
) {
}
