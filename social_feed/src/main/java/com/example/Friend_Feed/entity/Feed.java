package com.example.Friend_Feed.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "feed")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Feed {

    @Id
    private String id;
    private String feedOwnerId;
    private String authorId;
    private String postId;
    private Instant createdAt;

}
