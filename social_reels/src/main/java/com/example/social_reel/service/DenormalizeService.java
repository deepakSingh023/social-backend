package com.example.social_reel.service;


import com.example.social_reel.entity.Reel;
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
    public void denormalize(String userId, String avatar){

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));

        Update update = new Update();
        update.set("avatar",avatar);

        mongoTemplate.updateMulti(query,update, Reel.class);

    }


}
