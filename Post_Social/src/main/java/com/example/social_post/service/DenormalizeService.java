package com.example.social_post.service;


import com.example.social_post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;


import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DenormalizeService {

    private final MongoTemplate mongoTemplate;



    @Async("denormalize")
    public void avatarDenormalization(String userId, String avatar){

        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));

        Update update = new Update();

        update.set("avatar",avatar);

        mongoTemplate.updateMulti(query,update, Post.class);

    }
}
