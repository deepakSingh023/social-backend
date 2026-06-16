package com.example.social_interaction.dto;

import com.example.social_interaction.enums.CounterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateCounter(
        @NotBlank String userId,
        @NotNull CounterType type,
        @Pattern(regexp = "1|-1") int delta
) {}