package com.example.Friend_Feed.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection="posts")
public class Post {

    @Id
    private String id;

    private String userId;

    private String avatar;

    private String username;



    private List<String> imageUrls;
    private String videoUrl;

    private String caption;

    private String songUrl;
    private String songName;
    private String artistName;

    private List<String> tags;

    private boolean isPrivate;

    private Instant createdAt;

    private int likes;
    private int comments;
}

