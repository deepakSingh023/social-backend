package com.example.social_post;

public enum ImpressionType {
    LIKE("likes"),
    COMMENT("comments");

    private final String field;

    ImpressionType(String field){
        this.field= field;
    }

    public String getField(){
        return field;
    }
}
