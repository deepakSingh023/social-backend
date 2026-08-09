package com.example.social_reel.service;

import com.example.social_reel.dto.DenormalizeDto;
import com.example.social_reel.dto.DenormalizeEvent;
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
            topics = "profile-reel-events"
    )
    public void consume(DenormalizeEvent event) {

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        denormalizeService.denormalize(
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