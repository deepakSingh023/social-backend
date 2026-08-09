package com.example.social_post.dto;

public record DenormalizeEvent(

        String eventId,

        String aggregateId,

        DenormalizeDto payload

) {}