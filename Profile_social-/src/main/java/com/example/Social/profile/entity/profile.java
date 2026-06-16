package com.example.Social.profile.entity;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Document(collection = "profile")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class profile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String username;

    private String email;

    @Builder.Default
    private String profilePicUrl="";

    @Builder.Default
    private String bio="";

    @Builder.Default
    private Boolean privateAcc=false;

    @Builder.Default
    private long reels=0;

    @Builder.Default
    private long posts=0;


    @Builder.Default
    private long followerCounter=0;


    @Builder.Default
    private long followingCounter=0;

    @Builder.Default
    private long friendsCounter=0;

}
