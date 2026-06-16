package com.example.social_interaction.tasks;


import com.example.social_interaction.dto.ConversationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "chat" , url = "${chat-service}")
public interface ChatClient {

    @PostMapping("/api/conversation/create-conversation")
    void createConversation(
            @RequestBody ConversationDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );

    @DeleteMapping("/api/conversation/delete-conversation")
    void deleteConversation(
            @RequestBody ConversationDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
