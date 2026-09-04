package com.example.Friend_Feed.service;


import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.InteractionDto;
import com.example.Friend_Feed.dto.InteractionFeedEvent;
import com.example.Friend_Feed.dto.PostFeedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

// Idempotency guard:
// Kafka provides at-least-once delivery, so the same event may be delivered
// more than once. Redis stores processed event IDs to avoid repeating work.
// Redis provides best-effort deduplication for Kafka redelivery.
// The check and write are intentionally performed around successful
// processing so a failed operation does not permanently suppress Kafka retry.
// A concurrent duplicate can still pass the check simultaneously because
// hasKey() and set() are not atomic.

@Service
@RequiredArgsConstructor
public class FeedListenerService {

    private final FanOutOnWriteFeedService fanOutOnWriteFeedService;

    private final RedisTemplate<String,String> redisTemplate;

    private final static Logger log = LoggerFactory.getLogger(FeedListenerService.class);

    @KafkaListener(topics = "post-feed-events-create")
    public void handlePostEvent(PostFeedEvent event) {

        log.info(" RECEIVED POST CREATE EVENT: {}", event);

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        fanOutOnWriteFeedService.createFeed(new CreateFeed(event.authorId(), event.postId()));


        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }

    @KafkaListener(topics = "post-feed-events-delete")
    public void handlePostDeleteEvent(PostFeedEvent event) {

        log.info(" RECEIVED POST DELETE EVENT: {}", event);

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        fanOutOnWriteFeedService.deleteFeedPost(event.postId());

        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );
    }

    @KafkaListener(topics = "create-feed-interaction")
    public void handleInteractionEvent(InteractionFeedEvent event) {

        log.info(" RECEIVED INTERACTION CRETE EVENT: {}", event);

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        fanOutOnWriteFeedService.createFeedForInteraction(new InteractionDto(event.authorId(), event.feedOwnerId()));

        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );

    }

    @KafkaListener(topics = "delete-feed-interaction")
    public void handleInteractionDeleteEvent(InteractionFeedEvent event) {

        log.info(" RECEIVED INTERACTION DELETE EVENT: {}", event);

        Boolean exists = redisTemplate.hasKey(event.eventId());

        //we use Boolean.TRUE.equals because if we only check exists and if return null then it will cause a Null pointer exception Boolean.TRUE.equals prevents it
        if(Boolean.TRUE.equals(exists)){
            return;
        }

        fanOutOnWriteFeedService.deleteFeed(event.feedOwnerId(), event.authorId());

        redisTemplate.opsForValue().set(
                event.eventId(),
                "processed",
                Duration.ofDays(1)
        );
    }
}
