package com.example.social_likes.enums;


import lombok.Getter;

@Getter
public enum ImpressionType {
    LIKE("likesCount"),
    COMMENT("repliesCount");

    private final String field;

    ImpressionType(String field){
        this.field= field;
    }
}
