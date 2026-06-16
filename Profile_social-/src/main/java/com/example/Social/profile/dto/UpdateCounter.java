package com.example.Social.profile.dto;

import com.example.Social.profile.enums.CounterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateCounter(
        @NotBlank String userId,
        @NotNull CounterType type,
        @Pattern(regexp = "1|-1") int delta
) {}