package com.example.social_post.service;

import com.example.social_post.dto.CreateFeed;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class FeedAsyncService {

    private final FeedClient feedClient;



    private final TraceContextService traceContextService;

    private final OutboxRepository outboxRepository;


    @Async("feedCreate")
    public void createFeed(CreateFeed data){

        Instant now = Instant.now();

        String trace = traceContextService.currentTraceParent();

        Outbox outbox = Outbox.builder()
                .aggregateId(data.postId())
                .aggregateType("POST")
                .eventType(EventType.CREATE)
                .topic("post-feed-events-delete")
                .status(EventStatus.PENDING)
                .payload(data)
                .retryCount(1)
                .createdAt(now)
                .traceParent(trace)
                .build();

        outboxRepository.save(outbox);

    }
}
