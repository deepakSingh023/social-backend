package com.example.social_post.service;


import com.example.social_post.dto.ReelUpdate;
import com.example.social_post.util.ProfileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceUpdate {

    private final ProfileClient profileClient;

    @Value("${service.secret}")
    private String token;

    @Async
    public void denormProfile(ReelUpdate data){
        profileClient.updatePostCounter(token, data);

    }

    @Async
    public void denormProfileAdd(ReelUpdate data){
        profileClient.updatePostCounter(token,data);
    }


}
