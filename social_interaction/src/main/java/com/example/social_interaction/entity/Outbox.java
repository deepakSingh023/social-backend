package com.example.social_interaction.entity;

import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.enums.AggregateType;
import com.example.social_interaction.enums.EventStatus;
import com.example.social_interaction.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Data
@Document(collection = "outbox")
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {

    @Id
    private  String  id;

    private  String aggregateId;

    private AggregateType aggregateType;

    private EventType eventType;

    private  String topic;

    private EventStatus status;

    private Object payload;

    private  int retryCount;

    private Instant createdAt;

    private String traceParent;
}
