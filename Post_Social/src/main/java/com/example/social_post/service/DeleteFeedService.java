package com.example.social_post.service;


import com.example.social_post.dto.CreateFeed;
import com.example.social_post.dto.DeleteFeed;
import com.example.social_post.entity.Outbox;
import com.example.social_post.enums.EventStatus;
import com.example.social_post.enums.EventType;
import com.example.social_post.repository.OutboxRepository;
import com.example.social_post.util.FeedClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DeleteFeedService {

    private final TraceContextService traceContextService;

    private  final OutboxRepository outboxRepository;


    @Async("feedDelete")
    public void deleteFeed(String postId){

        Instant now = Instant.now();

        String trace = traceContextService.currentTraceParent();

        Outbox outbox = Outbox.builder()
                .aggregateId(postId)
                .aggregateType("POST")
                .eventType(EventType.DELETE)
                .topic("post-feed-events-delete")
                .status(EventStatus.PENDING)
                .payload(new DeleteFeed(postId))
                .retryCount(1)
                .createdAt(now)
                .traceParent(trace)
                .build();

        outboxRepository.save(outbox);
    }

}
