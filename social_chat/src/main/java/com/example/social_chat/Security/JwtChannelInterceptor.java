package com.example.social_chat.Security;

import com.example.social_chat.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // accessor can be null for non-STOMP internal messages
        if (accessor == null) return message;

        // Only authenticate on the CONNECT frame.
        // All other frames (SEND, SUBSCRIBE, etc.) inherit the Principal
        // that was set during CONNECT — no need to re-validate JWT each time.
        if (!StompCommand.CONNECT.equals(accessor.getCommand())) return message;

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or malformed Authorization header on STOMP CONNECT");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid or expired JWT on STOMP CONNECT");
        }

        String userId = jwtUtil.extractUserId(token);

        // Set the Principal on the WebSocket session.
        // Spring propagates this to every subsequent frame automatically.
        accessor.setUser(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );

        return message;
    }
}