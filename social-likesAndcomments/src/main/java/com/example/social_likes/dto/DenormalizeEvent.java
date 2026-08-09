package com.example.social_likes.dto;

public record DenormalizeEvent(

        String eventId,

        String aggregateId,

        DenormalizeDto payload

) {}