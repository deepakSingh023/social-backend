package com.example.social_interest.dto;


import lombok.Data;

import java.util.Set;

@Data
public class InterestRequest {

    private String userId;

    private String reelId;

    private String event;

    private Set<String> tags;
}
