package com.example.social_reel.service;


import com.example.social_reel.dto.IncrementDecDto;
import com.example.social_reel.entity.Reel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class IncAndDecService {

    private final MongoTemplate mongoTemplate;




    public void incrementLike(IncrementDecDto data){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(data.postId()));

        Update update = new Update();
        update.inc(data.type().getField(),data.num());

        mongoTemplate.updateFirst(query,update, Reel.class);

    }


}
