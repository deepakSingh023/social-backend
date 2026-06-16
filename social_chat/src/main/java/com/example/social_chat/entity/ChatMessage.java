package com.example.social_chat.entity;

import com.example.social_chat.enums.MessageType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    @Indexed(unique = true)
    private String messageId;
    private String senderId;
    private String conversationId;

    private MessageType type;
    private String content;

    private Instant createdAt;
    private boolean delivered;
}
