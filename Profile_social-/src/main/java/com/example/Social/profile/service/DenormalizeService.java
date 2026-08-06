package com.example.Social.profile.service;


import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.dto.DenormalizeEvent;
import com.example.Social.profile.entity.Outbox;
import com.example.Social.profile.enums.EventStatus;
import com.example.Social.profile.repository.OutboxRepository;
import com.example.Social.profile.tasks.CommentsClient;
import com.example.Social.profile.tasks.InteractionClient;
import com.example.Social.profile.tasks.PostClient;
import com.example.Social.profile.tasks.ReelClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class DenormalizeService {


    private final R2ImageService r2ImageService;


    private static final Logger log = LoggerFactory.getLogger(DenormalizeService.class);

    private final KafkaTemplate<String, DenormalizeEvent> kafkaTemplate;

    private final OutboxRepository outboxRepository;

    private final TraceContextService traceContextService;


    private final DenormalizeWorker worker;

    @Scheduled(fixedDelay = 1000)
    public void kafkaPublisher(){

        List<Outbox> data = outboxRepository.findTop100ByStatusOrderByCreatedAt(EventStatus.PENDING);

        for (Outbox event : data) {

            DenormalizeEvent eve = new DenormalizeEvent(
                    event.getId(),
                    event.getAggregateId(),
                    event.getPayload()
            );

            try {

                Context context =
                        traceContextService.restore(
                                event.getTraceParent()
                        );


                try (Scope ignored = context.makeCurrent()) {

                    kafkaTemplate.send(
                            event.getTopic(),
                            event.getAggregateId(),
                            eve
                    ).get();

                }

                event.setStatus(EventStatus.SUCCEED);

            } catch (Exception e) {

                event.setRetryCount(event.getRetryCount() + 1);

                log.error("Kafka publish failed id={}",
                        event.getId(),
                        e);
            }

        }

        outboxRepository.saveAll(data);

    }




    @Async("denormalize")
    public void deleteOldImageAsync(String oldUrl) {
        try {
            r2ImageService.deleteImage(oldUrl);
        } catch (Exception e) {
            log.warn("delete old image failed oldUrl={}", oldUrl, e);
        }
    }

    public void fallback(
            DenormalizeDto data,
            Throwable ex
    ){
        log.error("profile creation failed after retries for user = {}",
                data.userId(),
                ex);

    }
}
