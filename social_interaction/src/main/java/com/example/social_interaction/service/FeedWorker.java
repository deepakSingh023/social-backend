package com.example.social_interaction.service;


import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.entity.Outbox;
import com.example.social_interaction.enums.AggregateType;
import com.example.social_interaction.enums.EventStatus;
import com.example.social_interaction.enums.EventType;
import com.example.social_interaction.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

// Outbox is persisted synchronously after the business write.
// This avoids returning success before the event is durably queued.
// The business write and outbox write are not currently one atomic
// MongoDB transaction; this is an accepted consistency tradeoff.
@RequiredArgsConstructor
@Service
public class FeedWorker {

    private final OutboxRepository outboxRepository;

    private final static Logger log = LoggerFactory.getLogger(FeedWorker.class);

    private final TraceContextService traceContextService;

    public void createFeedWorker(InteractionDto data){

        Instant time = Instant.now();

        String trace = traceContextService.currentTraceParent();

        Outbox outbox = Outbox.builder()
                .aggregateId(data.feedOwnerId())
                .aggregateType(AggregateType.INTERACTION)
                .eventType(EventType.CREATE)
                .topic("create-feed-interaction")
                .status(EventStatus.PENDING)
                .payload(data)
                .retryCount(0)
                .createdAt(time)
                .traceParent(trace)
                .build();


        outboxRepository.save(outbox);

    }

    public void deleteFeedWorker(InteractionDto data){

        Instant time = Instant.now();

        String trace = traceContextService.currentTraceParent();

        Outbox outbox = Outbox.builder()
                .aggregateId(data.feedOwnerId())
                .aggregateType(AggregateType.INTERACTION)
                .eventType(EventType.DELETE)
                .topic("delete-feed-interaction")
                .status(EventStatus.PENDING)
                .payload(data)
                .retryCount(0)
                .createdAt(time)
                .traceParent(trace)
                .build();


        outboxRepository.save(outbox);

    }

}
