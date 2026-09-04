package com.example.social_post.service;

import com.example.social_post.dto.DenormalizeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ProfileEventListener {

    private final DenormalizeService denormalizeService;

    private final RedisTemplate<String,String> redisTemplate;

    //The avatar update is naturally idempotent because it overwrites the same value. I implemented Redis idempotency to avoid unnecessary MongoDB writes and reduce server load in case of duplicate Kafka deliveries.
    @KafkaListener(
            topics = "profile-post-events",
            groupId = "post-service-group"
    )
    public void consume(DenormalizeEvent event) {

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it 
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        denormalizeService.avatarDenormalization(
                event.payload().userId(),
                event.payload().avatar()
        );


        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }
}