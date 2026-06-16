package com.example.social_likes.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comments {

    @Id
    private String id;

    private String postId;

    // null → top-level comment
    private String parentCommentId;

    private String userId;
    private String username;
    private String userAvatar;

    private String content;

    private long likesCount;
    private long repliesCount;


    private Instant createdAt;
    private Instant updatedAt;
}
