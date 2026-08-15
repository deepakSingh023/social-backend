package com.example.social_interaction.service;

import com.example.social_interaction.dto.ConversationDto;
import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.dto.InteractionFeedEvent;
import com.example.social_interaction.entity.Outbox;
import com.example.social_interaction.enums.AggregateType;
import com.example.social_interaction.enums.EventStatus;
import com.example.social_interaction.repository.OutboxRepository;
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
public class FeedProducer {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TraceContextService traceContextService;
    private final ObjectMapper objectMapper;

    private static final Logger log =
            LoggerFactory.getLogger(FeedProducer.class);

    @Scheduled(fixedDelay = 1000)
    public void KafkaPublisher() {

        List<Outbox> data =
                outboxRepository.findTop100ByStatusOrderByCreatedAt(
                        EventStatus.PENDING
                );

        for (Outbox event : data) {

            try {
                Context context =
                        traceContextService.restore(
                                event.getTraceParent()
                        );

                try (Scope ignored = context.makeCurrent()) {

                    Object eve;

                    if (event.getAggregateType() == AggregateType.INTERACTION) {

                        InteractionDto payload =
                                objectMapper.convertValue(
                                        event.getPayload(),
                                        InteractionDto.class
                                );

                        eve = new InteractionFeedEvent(
                                event.getId(),
                                payload.authorId(),
                                payload.feedOwnerId()
                        );

                    } else if (event.getAggregateType() == AggregateType.CONVERSATION) {

                        ConversationDto payload =
                                objectMapper.convertValue(
                                        event.getPayload(),
                                        ConversationDto.class
                                );

                        eve = new ConversationDto(
                                event.getId(),
                                payload.user1Id(),
                                payload.user2Id()
                        );

                    } else {

                        log.error(
                                "Unknown aggregate type {} for event {}",
                                event.getAggregateType(),
                                event.getId()
                        );

                        continue;
                    }

                    kafkaTemplate.send(
                            event.getTopic(),
                            event.getAggregateId(),
                            eve
                    ).get();
                }

                event.setStatus(EventStatus.SUCCEED);

            } catch (Exception e) {

                event.setRetryCount(
                        event.getRetryCount() + 1
                );

                log.error(
                        "Kafka publish failed id={}",
                        event.getId(),
                        e
                );
            }
        }

        outboxRepository.saveAll(data);
    }
}