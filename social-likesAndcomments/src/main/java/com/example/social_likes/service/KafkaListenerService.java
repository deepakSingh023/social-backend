package com.example.social_likes.service;

import com.example.social_likes.dto.DenormalizeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;


@RequiredArgsConstructor
@Service
public class KafkaListenerService {

    private final RedisTemplate<String,String> redisTemplate;

    private  final  DenormalizeService denormalizeService;

    @KafkaListener(
            topics = "profile-comment-events"
    )
    public void consume(DenormalizeEvent event) {

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        denormalizeService.denormalizeCommentAvatar(
                event.payload().avatar(),
                event.payload().userId()
        );


        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }
}
