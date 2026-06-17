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
public class CreateCommentDTO {

    private String postId;

    private String parentCommentId;

    private String content;

    private LikeTargetType commentType;
}
