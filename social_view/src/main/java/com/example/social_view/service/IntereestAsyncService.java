package com.example.social_view.service;

import com.example.social_view.dto.InterestDto;
import com.example.social_view.util.InterestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IntereestAsyncService {

    private final InterestClient interestClient;

    @Value("${service.secret}")
    private String secret;

    @Async
    public void updateInterest(InterestDto data){

        interestClient.sendInterest(data,secret);

    }
}
