package com.example.Social.profile.dto;
import lombok.*;


@Data
@AllArgsConstructor
public class fetchProfile {

    private String userId;

    private String username;

    private String bio;

    private String email;

    private String profilePicUrl;

    private Boolean privateAcc;

    private long reels;

    private long posts;

    private  long followerCounter;

    private  long followingCounter;

    private  long friendsCounter;



}

