package com.example.social_likes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private String id;
    private String postId;
    private String parentCommentId;

    private String userId;

    private String content;

    private String username;
    private String userAvatar;

    private long likesCount;
    private long repliesCount;

    private boolean likedByCurrentUser;

    @JsonProperty("isOwner")
    private boolean isOwner;

    private Instant createdAt;
}
