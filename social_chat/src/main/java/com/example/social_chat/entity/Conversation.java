package com.example.social_chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("conversation")
@CompoundIndex(
        def = "{'userId1':1,'userId2':1}",
        unique = true
)
public class Conversation {

    private  String id;

    private String userId1;

    private String userId2;

    private Instant createdAt;
}
