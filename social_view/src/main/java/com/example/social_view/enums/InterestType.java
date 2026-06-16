package com.example.social_view.enums;


import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum InterestType {
    WATCH_50,
    WATCH_90,
    LIKE
}
