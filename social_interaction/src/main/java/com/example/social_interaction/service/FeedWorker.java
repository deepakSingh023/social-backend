package com.example.social_interaction.service;


import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.dto.UpdateCounter;
import com.example.social_interaction.tasks.PostClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class FeedWorker {

    private final PostClient postClient;

    private final static Logger log = LoggerFactory.getLogger(FeedWorker.class);

    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    @Async
    public void createFeedWorker(InteractionDto data, String secret){
        postClient.createFeed(data,secret);
    }

    public void fallback(
            InteractionDto data,
            String secret,
            Throwable ex
    ){

        log.error("feed cannot be generated for this user = {}",data.feedOwnerId(),ex);

    }
}
