package com.example.gateway.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public AsyncProxyManager<String> lettuceProxyManager(RedisConnectionFactory connectionFactory) {

        if (!(connectionFactory instanceof LettuceConnectionFactory lettuceFactory)) {
            throw new IllegalStateException("Spring Cloud MVC Gateway requires a LettuceConnectionFactory.");
        }

        RedisClient redisClient = (RedisClient) lettuceFactory.getNativeClient();
        if (redisClient == null) {
            throw new IllegalStateException("Native Lettuce RedisClient could not be extracted.");
        }

        // THE DEFINITIVE FIX: Create a custom codec matching String keys with byte[] values
        RedisCodec<String, byte[]> stringKeyAndByteValueCodec = RedisCodec.of(
                StringCodec.UTF8,
                ByteArrayCodec.INSTANCE
        );

        // Open the stateful connection using our dual-type codec
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(stringKeyAndByteValueCodec);

        // Build the manager. The types now align perfectly with the builder expectations
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofDays(1)))
                .build()
                .asAsync();
    }
}
