package com.example.social_reel.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("reels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reel {

    @Id
    private String id;

    private String userId;

    private String username;

    private String avatar;


    private String caption;

    private String videoUrl;

    private List<String> rawTags;

    private Set<String> semanticTags;

    private long viewCount;

    private double popularityScore;

    private long likes = 0;

    private long comments = 0;

    private Instant createdAt;
}
