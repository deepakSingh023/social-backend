package com.example.Social.profile.service;

import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.tasks.CommentsClient;
import com.example.Social.profile.tasks.InteractionClient;
import com.example.Social.profile.tasks.PostClient;
import com.example.Social.profile.tasks.ReelClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class DenormalizeWorker {

    private final PostClient postClient;
    private final ReelClient reelClient;
    private final InteractionClient interactionClient;
    private final CommentsClient commentsClient;

    private static final Logger log = LoggerFactory.getLogger(DenormalizeWorker.class);

    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    public void denormPost(DenormalizeDto data, String secret){
        postClient.denormalizePost(data, secret);
    }
    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    public void denormReel(DenormalizeDto data,String secret){
        reelClient.denormalize(data, secret);

    }
    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    public void denormComment(DenormalizeDto data,String secret){
        commentsClient.denormalizePost(data,secret);
    }
    @Retry(name="importantApi")
    @CircuitBreaker(name="importantApi",
            fallbackMethod = "fallback")
    public void denormInteraction(DenormalizeDto data,String secret){
        interactionClient.denormalize(data, secret);
    }

    public void fallback(
            DenormalizeDto data,
            String secret,
            Throwable ex
    ){
        log.error("profile creation failed after retries for user = {}",
                data.userId(),
                ex);

    }
}
