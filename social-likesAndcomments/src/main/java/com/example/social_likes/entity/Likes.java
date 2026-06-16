package com.example.social_likes.entity;


import com.example.social_likes.enums.LikeTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "likes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Likes{

    @Id
    private String id;

    private String targetId;   // postId OR commentId
    private LikeTargetType targetType;

    private String userId;

    private Instant createdAt;
}
