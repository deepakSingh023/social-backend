package com.example.social_chat.controller;

import com.example.social_chat.entity.ChatMessage;
import com.example.social_chat.repository.ChatMessageRepository;
import com.example.social_chat.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository repo;
    private final ConversationService conversationService;


    @GetMapping("/get-chat")
    public Page<ChatMessage> getChat(
            @RequestParam String conversationId,
            @RequestParam int page,
            @RequestParam int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return repo.findByConversationIdOrderByCreatedAtDesc(
                conversationId, pageable);
    }


    @GetMapping("/get-convoId")
    public ResponseEntity<String> getConversation(
            @RequestParam String receiverId,
            @RequestHeader("X-User-Id") String userId
    ){

        String conversationId = conversationService.getConvId(userId,receiverId);

        return ResponseEntity.ok().body(conversationId);
    }

}
