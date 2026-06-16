package com.example.social_interaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "interaction")
public class Feed {

    @Id
    private String id;

    private String authorId;

    private String recipientUserId;

    private Instant createdAt;
}
