package com.example.social_chat.redis;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatPublisher {

    private final StringRedisTemplate redis;
    private final RedisTemplate<String,String> redisTemplate;


    @Value("${INSTANCE_NAME}")
    private String instanceName;

    private static final Logger log = LoggerFactory.getLogger(ChatPublisher.class);


    public void publish(String messageJson, String conversationId,String senderId) {

        Set<String> users = redisTemplate.opsForSet()
                .members("Conversation:" + conversationId);

        if (users == null || users.isEmpty()) {
            return;
        }

        String receiverId = users.stream()
                .filter(userId -> !userId.equals(senderId))
                .findFirst()
                .orElse(null);

        if (receiverId == null) {
            return;
        }

        String receiverInstance = (String) redisTemplate.opsForHash()
                .get("User:" + receiverId, "instance");

        if (receiverInstance == null) {
            return;
        }

        redis.convertAndSend(
                "chat-channel:" + receiverInstance,
                messageJson
        );

        log.info(
                "[{}] Published message to instance={} receiver={}",
                instanceName,
                receiverInstance,
                receiverId
        );
    }


}
