package com.example.social_post.service;


import com.example.social_post.ImpressionType;
import com.example.social_post.dto.IncrementDecDto;
import com.example.social_post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikesAndCommentService {

    private final MongoTemplate mongoTemplate;


    public void incrementDecrement(IncrementDecDto data) {

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(data.postId()));

        Update update = new Update();

        update.inc(data.type().getField(), data.num());

        mongoTemplate.updateFirst(query, update, Post.class);
    }


}


