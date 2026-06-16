package com.example.social_chat.controller;

import com.example.social_chat.dto.ChatRequest;
import com.example.social_chat.services.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatSocketController {

    private final ChatService chatService;

    private static final Logger log = LoggerFactory.getLogger(ChatSocketController.class);

    public ChatSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Value("${INSTANCE_NAME}")
    private String instanceName;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatRequest request, Principal principal) {

        log.info(
                "[{}] Processing message from user={}",
                instanceName,
                principal.getName()
        );

        chatService.processMessage(request, principal.getName());
    }
}
