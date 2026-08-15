package com.example.social_chat.services;


import com.example.social_chat.dto.ConversationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final RedisTemplate<String, String> redisTemplate;

    private final ConversationService conversationService;

    @KafkaListener(topics = "conversation-create")
    public void listenerCreate(ConversationDto data){

        Boolean exists = redisTemplate.hasKey(data.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        conversationService.createCOnvo(data);


        redisTemplate.opsForValue().set(
                data.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }

    @KafkaListener(topics = "conversation-delete")
    public void listenerDelete(ConversationDto data){

        Boolean exists = redisTemplate.hasKey(data.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        conversationService.deleteConvo(data);


        redisTemplate.opsForValue().set(
                data.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }
}
