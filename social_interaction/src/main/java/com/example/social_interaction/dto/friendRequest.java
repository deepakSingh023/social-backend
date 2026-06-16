package com.example.social_interaction.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class friendRequest{

    private String receiverId;


    private String receiverName;

    private String receiverAvatar;
}
