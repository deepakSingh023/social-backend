package com.example.social_interaction.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "followers")

public class Follower {

    @Id
    private String id;

    @Indexed
    private String userId;//me

    private String userAvatar;

    private String userName;


    @Indexed
    private String followedId;//the person being followed

    private String followedAvatar;

    private String followedName;

    private Instant createdAt;

}
