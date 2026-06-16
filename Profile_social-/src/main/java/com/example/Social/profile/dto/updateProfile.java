package com.example.Social.profile.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class updateProfile {

    @Size(max = 500, message = "Bio must be at most 150 characters")
    private String bio;

    private String profilePicUrl;

    private Boolean privateAcc;
}


