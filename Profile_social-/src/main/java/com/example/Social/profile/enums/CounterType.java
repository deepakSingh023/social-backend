package com.example.Social.profile.enums;




public enum CounterType {

    FOLLOWER("followerCounter"),
    FOLLOWING("followingCounter"),
    FRIENDS("friendsCounter");


    private final String field;

    CounterType(String field) {
        this.field = field;
    }


    public String getField(){
        return field;
    }
}
