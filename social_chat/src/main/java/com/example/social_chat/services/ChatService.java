package com.example.social_chat.services;

import com.example.social_chat.dto.ChatRequest;
import com.example.social_chat.entity.ChatMessage;
import com.example.social_chat.redis.ChatPublisher;
import com.example.social_chat.repository.ChatMessageRepository;
import com.example.social_chat.repository.ConversationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository repository;
    private final UserPresenceService presenceService;
    private final ChatPublisher publisher;
    private final ObjectMapper mapper;
    private final ConversationService conversationService;


    public void processMessage(ChatRequest request, String senderId) {

        ChatMessage message = ChatMessage.builder()
                .messageId(request.getMessageId())
                .senderId(senderId)
                .conversationId(request.getConversationId())
                .type(request.getType())
                .content(request.getContent())
                .createdAt(Instant.now())
                .delivered(false)
                .build();

        try {
            repository.save(message);
        } catch (Exception e) {
            return;
        }

        try {
            publisher.publish(
                    mapper.writeValueAsString(message)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
