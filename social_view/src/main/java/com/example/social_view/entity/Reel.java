package com.example.social_view.entity;

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

    private String videoUrl;

    // what user typed
    private List<String> rawTags;

    // semantic meaning buckets
    private Set<String> semanticTags;

    private long viewCount;
    private double popularityScore;

    private Instant createdAt;
}
