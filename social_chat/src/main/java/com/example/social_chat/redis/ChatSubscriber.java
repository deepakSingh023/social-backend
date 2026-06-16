package com.example.social_chat.redis;

import com.example.social_chat.entity.ChatMessage;
import com.example.social_chat.services.UserPresenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper mapper;

    @Value("${INSTANCE_NAME}")
    private String instanceName;

    private static final Logger log = LoggerFactory.getLogger(ChatSubscriber.class);


    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ) {
        try {

            ChatMessage chat =
                    mapper.readValue(
                            message.getBody(),
                            ChatMessage.class
                    );

            messagingTemplate.convertAndSend(
                    "/topic/conversation/" +
                            chat.getConversationId(),
                    chat
            );

            log.info(
                    "[{}] Received conversation={} from Redis",
                    instanceName,
                    chat.getConversationId()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
