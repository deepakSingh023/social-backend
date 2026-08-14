package com.example.social_chat.services;


import com.example.social_chat.utils.InstanceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;


@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final InstanceInfo instanceInfo;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();

        if (principal == null) {
            return;
        }

        String userId = principal.getName();
        String instanceId = instanceInfo.getInstanceId();

        redisTemplate.opsForHash().put(
                "User:" + userId,
                "instance",
                instanceId
        );

        String sessionId = accessor.getSessionId();

        redisTemplate.opsForValue().set(
                "SessionId:" + sessionId,
                userId
        );

    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        if (sessionId == null) {
            return;
        }

        String userId = redisTemplate.opsForValue().get(
                "SessionId:" + sessionId
        );

        if (userId == null) {
            return;
        }

        String conversationId = (String) redisTemplate.opsForHash()
                .get("User:" + userId, "conversation");

        if (conversationId != null) {
            redisTemplate.opsForSet().remove(
                    "Conversation:" + conversationId,
                    userId
            );
        }

        redisTemplate.delete("SessionId:" + sessionId);
        redisTemplate.delete("User:" + userId);
    }
}
