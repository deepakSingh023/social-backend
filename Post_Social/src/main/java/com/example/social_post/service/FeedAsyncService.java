package com.example.social_post.service;

import com.example.social_post.dto.CreateFeed;
import com.example.social_post.util.FeedClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FeedAsyncService {

    private final FeedClient feedClient;

    private final static Logger log = LoggerFactory.getLogger(FeedAsyncService.class);

    @Retry(name= "importantApi")
    @CircuitBreaker(name = "importantApi",
    fallbackMethod = "fallback")
    @Async("feedCreate")
    public void createFeed(CreateFeed data , String token){

        feedClient.createFeed(token,data);

    }

    public void fallback(
            CreateFeed data ,
            String token  ,
            Throwable ex
    ){

        log.error(" creation of feed creation failed for user = {} and post = {}",data.userId(),data.postId(),ex);

    }
}
