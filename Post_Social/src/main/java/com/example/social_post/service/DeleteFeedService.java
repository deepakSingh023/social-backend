package com.example.social_post.service;


import com.example.social_post.dto.CreateFeed;
import com.example.social_post.util.FeedClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFeedService {

    private final FeedClient feedClient;

    @Value("${service.secret}")
    private String token;

    private final static Logger log = LoggerFactory.getLogger(DeleteFeedService.class);


    @Retry(name= "importantApi")
    @CircuitBreaker(name = "importantApi",
            fallbackMethod = "fallback")
    @Async("feedDelete")
    public void deleteFeed(String postId){

        feedClient.deleteFeedPost(postId,token);
    }

    public void fallback(
            String postId,
            Throwable ex
    ){

        log.error("deletion of feed creation failed for post = {}",postId,ex);

    }
}
