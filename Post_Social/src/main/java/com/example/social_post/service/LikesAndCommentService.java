package com.example.social_post.service;


import com.example.social_post.ImpressionType;
import com.example.social_post.dto.CreateFeed;
import com.example.social_post.dto.IncrementDecDto;
import com.example.social_post.entity.Post;
import com.example.social_post.util.LikeClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikesAndCommentService {

    private final MongoTemplate mongoTemplate;

    private final LikeClient likeClient;

    private final static Logger log = LoggerFactory.getLogger(LikesAndCommentService.class);

    @Value("${service.secret}")
    private String token;

    public void incrementDecrement(IncrementDecDto data) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(data.postId()));

        Update update = new Update();

        update.inc(data.type().getField(), data.num());

        mongoTemplate.updateFirst(query, update, Post.class);
    }


    @Retry(name = "importantApi")
    @CircuitBreaker(
            name = "importantApi",
            fallbackMethod = "fallback"
    )
    @Async
    public void removeLikesAndComment(String postId){
        likeClient.deleteLikesAndComments(postId,token);
    }

    public void fallback(
            String postId,
            Throwable ex
    ){
        log.error("cleanup failed for post={}", postId, ex);
    }


}


