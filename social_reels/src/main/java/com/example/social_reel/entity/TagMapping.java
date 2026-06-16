package com.example.social_reel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("tag_mappings")
@Data
public class TagMapping {

    @Id
    private String id;

    private String rawTag;

    private String semanticTag;

    private double confidence;
}
