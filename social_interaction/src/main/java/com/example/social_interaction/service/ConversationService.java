package com.example.social_interaction.service;


import com.example.social_interaction.dto.ConversationDto;
import com.example.social_interaction.dto.ConversationEvent;
import com.example.social_interaction.entity.Outbox;
import com.example.social_interaction.enums.AggregateType;
import com.example.social_interaction.enums.EventStatus;
import com.example.social_interaction.enums.EventType;
import com.example.social_interaction.repository.OutboxRepository;
import com.example.social_interaction.tasks.ChatClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;


@RequiredArgsConstructor
@Service
public class ConversationService {


    private final ChatClient chatClient;

    private final static Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final TraceContextService traceContextService;

    private final OutboxRepository outboxRepository;



    public void createConversation(String senderId, String receiverId){

        ConversationEvent conversationDto = new ConversationEvent(senderId,receiverId);

        String trace = traceContextService.currentTraceParent();



        Outbox outbox = Outbox.builder()
                .aggregateId(senderId)
                .aggregateType(AggregateType.CONVERSATION)
                .eventType(EventType.CREATE)
                .topic("create-conversation")
                .status(EventStatus.PENDING)
                .payload(conversationDto)
                .retryCount(0)
                .createdAt(Instant.now())
                .traceParent(trace)
                .build();


        outboxRepository.save(outbox);
    }



    public void deleteConversation(String senderId, String receiverId){

        ConversationEvent conversationDto = new ConversationEvent(senderId,receiverId);

        String trace = traceContextService.currentTraceParent();



        Outbox outbox = Outbox.builder()
                .aggregateId(senderId)
                .aggregateType(AggregateType.CONVERSATION)
                .eventType(EventType.DELETE)
                .topic("delete-conversation")
                .status(EventStatus.PENDING)
                .payload(conversationDto)
                .retryCount(0)
                .createdAt(Instant.now())
                .traceParent(trace)
                .build();


        outboxRepository.save(outbox);

    }

}
