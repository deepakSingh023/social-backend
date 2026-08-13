package com.example.social_interaction.service;

import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.entity.Outbox;
import com.example.social_interaction.enums.EventStatus;
import com.example.social_interaction.repository.OutboxRepository;
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

    private final KafkaTemplate<String, InteractionDto> kafkaTemplate;

    private final TraceContextService traceContextService;

    private final static Logger log = LoggerFactory.getLogger(FeedProducer.class);


    @Scheduled(fixedDelay = 1000)
    public void KafkaPublisher(){

        List<Outbox> data = outboxRepository.findTop100ByStatusOrderByCreatedAt(EventStatus.PENDING);

        for(Outbox event : data){

            try {
                Context context =
                        traceContextService.restore(
                                event.getTraceParent()
                        );


                try (Scope ignored = context.makeCurrent()) {

                    kafkaTemplate.send(
                            event.getTopic(),
                            event.getAggregateId(),
                            event.getPayload()
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
}
