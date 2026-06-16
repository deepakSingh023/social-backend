package com.example.social_chat.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.social_chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatMessageRepository
        extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(
            String conversationId,
            Pageable pageable
    );
}
