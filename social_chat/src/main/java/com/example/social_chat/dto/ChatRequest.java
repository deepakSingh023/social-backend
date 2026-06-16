package com.example.social_chat.dto;

import com.example.social_chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    private String messageId;   // UUID from frontend (deduplication)
    private String conversationId;

    private MessageType type;   // TEXT, IMAGE, VIDEO

    private String content;     // text OR media URL
}
