package com.example.social_interaction.service;


import com.example.social_interaction.dto.ConversationDto;
import com.example.social_interaction.tasks.ChatClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class ConversationService {


    private final ChatClient chatClient;

    private final static Logger log = LoggerFactory.getLogger(ConversationService.class);

    @Value("${service.secret}")
    private String token;


    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
    fallbackMethod = "fallback")
    @Async("conversationUpdate")
    public void createConversation(String senderId, String receiverId){

        ConversationDto conversationDto = new ConversationDto(senderId,receiverId);

        chatClient.createConversation(conversationDto,token);

    }

    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    @Async("conversationUpdate")
    public void deleteConversation(String senderId, String receiverId){

        ConversationDto conversationDto = new ConversationDto(senderId,receiverId);

        chatClient.deleteConversation(conversationDto,token);

    }

    public void fallback(
            String senderId,
            String receiverId,
            Throwable ex
    ){
        log.error("the api for conversation creation or delete in not workign for this user1 = {} and user2 = {}",
                senderId,
                receiverId,
                ex);
    }
}
