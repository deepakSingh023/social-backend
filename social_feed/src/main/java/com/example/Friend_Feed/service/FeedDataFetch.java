package com.example.Friend_Feed.service;


import com.example.Friend_Feed.aspect.LogAspect;
import com.example.Friend_Feed.dto.RecipientPage;
import com.example.Friend_Feed.dto.RecipientsPosts;
import com.example.Friend_Feed.utils.InteractionClient;
import com.example.Friend_Feed.utils.PostClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class FeedDataFetch {

    private final InteractionClient interactionClient;

    private final PostClient postClient;

    public static final Logger log  = LoggerFactory.getLogger(FeedDataFetch.class);

    @Retry(name="importantApi")
    @CircuitBreaker(name = "importantApi",
    fallbackMethod = "fallback")
    public RecipientPage getInteractionData(String userId,String cursor, int size, String token){

        RecipientPage page =  interactionClient.getInteractionIds(userId,cursor,size,token);

        return page;

    }

    public RecipientPage fallback(
            String userId,
            String cursor,
            int size,
            String token,
            Throwable ex
    ) {
        log.error("interaction fetch data fail for user = {}", userId, ex);

        return new RecipientPage(
                List.of(),
                null
        );
    }

    @Retry(name="importantApi")
    @CircuitBreaker(name = "importantApi",
            fallbackMethod = "fallback2")
    public RecipientsPosts getPostData(String token,String authorId, int size, String cursor){

        RecipientsPosts posts = postClient.getPosts(token,authorId,size,cursor);

        return posts;

    }

    public RecipientsPosts fallback2(
            String token,String authorId, int size, String cursor,
            Throwable ex
    ){
        log.error("interaction fetch data fail for user = {}",authorId,ex);

        return new RecipientsPosts(
                List.of(),
                null
        );

    }




}
