package com.example.social_post.service;


import com.example.social_post.dto.CreateFeed;
import com.example.social_post.dto.DeleteFeed;
import com.example.social_post.dto.PostDeleteEvent;
import com.example.social_post.dto.PostFeedEvent;
import com.example.social_post.entity.Outbox;
import com.example.social_post.enums.EventStatus;
import com.example.social_post.enums.EventType;
import com.example.social_post.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaProducer {


    private final OutboxRepository outboxRepository;

    private final TraceContextService traceContextService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final static Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Scheduled(fixedDelay = 1000)
    public void kafkaPublisher(){

        List<Outbox> data = outboxRepository.findTop100ByStatusOrderByCreatedAt(EventStatus.PENDING);

        for (Outbox event : data) {

            if(event.getEventType().equals(EventType.CREATE)){

                CreateFeed payload = objectMapper.convertValue(
                        event.getPayload(),
                        CreateFeed.class
                );

                PostFeedEvent eve = new PostFeedEvent(
                        event.getId(),
                        payload.postId(),
                        payload.userId()
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


            } else if (event.getEventType().equals(EventType.DELETE)) {

                DeleteFeed payload = objectMapper.convertValue(
                        event.getPayload(),
                        DeleteFeed.class
                );

                PostDeleteEvent eve = new PostDeleteEvent(
                        event.getId(),
                        payload.postId()
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


        }

        outboxRepository.saveAll(data);
    }
}


