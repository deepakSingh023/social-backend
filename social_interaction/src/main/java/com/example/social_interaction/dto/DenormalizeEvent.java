package com.example.social_interaction.dto;

public record DenormalizeEvent(

        String eventId,

        String aggregateId,

        DenormalizeDto payload

) {}