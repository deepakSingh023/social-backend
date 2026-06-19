package com.example.social_interest.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "user_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private Map<String, InterestScore> interests = new HashMap<>();

    private Instant lastUpdated;
}

