package com.example.social_likes.dto;

import com.example.social_likes.enums.LikeTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDTO {
    private String targetId;
    private LikeTargetType targetType;
    private boolean liked;
}
