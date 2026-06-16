package com.example.social_chat.redis;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPublisher {

    private final StringRedisTemplate redis;


    @Value("${INSTANCE_NAME}")
    private String instanceName;

    private static final Logger log = LoggerFactory.getLogger(ChatPublisher.class);

    private static final String CHANNEL = "chat-channel";

    public void publish(String messageJson) {

        redis.convertAndSend(CHANNEL, messageJson);

        log.info(
                "[{}] Published message to Redis",
                instanceName
        );
    }


}
