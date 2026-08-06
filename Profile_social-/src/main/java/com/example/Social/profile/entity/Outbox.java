package com.example.Social.profile.entity;


import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.enums.EventStatus;
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

    private  String aggregateType;

    private  String eventType;

    private  String topic;

    private EventStatus status;

    private DenormalizeDto payload;

    private  int retryCount;

    private Instant createdAt;

    private String traceParent;
}
