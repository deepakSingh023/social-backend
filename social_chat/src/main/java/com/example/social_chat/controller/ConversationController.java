package com.example.social_chat.controller;


import com.example.social_chat.dto.ConversationDto;
import com.example.social_chat.services.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/create-conversation")
    public ResponseEntity<Void> createConversation(
            @RequestBody ConversationDto data
            ){

        conversationService.createCOnvo(data);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-conversation")
    public ResponseEntity<Void> deleteConversation(
            @RequestBody ConversationDto data
    ){

        conversationService.deleteConvo(data);

        return ResponseEntity.ok().build();
    }

}
