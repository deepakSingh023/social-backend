package com.example.Social.profile.service;

import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.entity.Outbox;
import com.example.Social.profile.enums.EventStatus;
import com.example.Social.profile.repository.OutboxRepository;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
public class DenormalizeWorker {

    private  final OutboxRepository outboxRepository;
    private final TraceContextService traceContextService;

    private static final Logger log = LoggerFactory.getLogger(DenormalizeWorker.class);

    private final static List<String> topics = List.of(
            "profile-post-events",
            "profile-reel-events",
            "profile-comment-events",
            "profile-interaction-events"
    );

    @Async("denormalize")
    public void denormalizeOutboxAgent(DenormalizeDto data ){

        List<Outbox> outboxes = new ArrayList<>(topics.size());

        Instant now = Instant.now();

        String traceParent = traceContextService.currentTraceParent();


        for(String topic : topics){
            Outbox outbox = Outbox.builder()
                    .aggregateId(data.userId())
                    .aggregateType("PROFILE")
                    .eventType("DENORMALIZATION")
                    .topic(topic)
                    .status(EventStatus.PENDING)
                    .payload(data)
                    .retryCount(0)
                    .createdAt(now)
                    .traceParent(traceParent)
                    .build();

            outboxes.add(outbox);
        }

        outboxRepository.saveAll(outboxes);

    }
}
