package com.example.Social.profile.dto;

public record DenormalizeEvent(

        String eventId,

        String aggregateId,

        DenormalizeDto payload

) {}