package com.example.social_reel.dto;

import java.util.List;

public record CreateReel(
        String videoUrl,
        String caption,
        List<String> tags
) {
}
