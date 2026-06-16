package com.example.social_reel.service;


import com.example.social_reel.entity.Reel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ViewIncrement {

    private final MongoTemplate mongoTemplate;

    public Reel incrementView(String reelId){

        Query query = Query.query(Criteria.where("_id").is(reelId));

        Update update = new Update().inc("viewCount",1);

        return mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Reel.class
        );
    }
}
