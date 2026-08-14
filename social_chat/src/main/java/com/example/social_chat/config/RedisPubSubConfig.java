package com.example.social_chat.config;
import com.example.social_chat.redis.ChatSubscriber;
import com.example.social_chat.utils.InstanceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final RedisConnectionFactory connectionFactory;
    private final ChatSubscriber chatSubscriber;
    private final InstanceInfo instanceInfo;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                chatSubscriber,
                new PatternTopic("chat-channel:"  + instanceInfo.getInstanceId())
        );

        return container;
    }
}
