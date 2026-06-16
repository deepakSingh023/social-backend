package com.example.social_interaction.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "friend_requests")
public class  FriendRequest {

    @Id
    private String id;


    @Indexed
    private String senderId;


    private String senderAvatar;

    private String senderName;


    @Indexed
    private String receiverId;


    private String receiverName;

    private String receiverAvatar;



    private Instant receivedAt;


}
