package com.example.social_reel.dto;


public record DenormalizeEvent(

        String eventId,

        String aggregateId,

        DenormalizeDto payload

) {}