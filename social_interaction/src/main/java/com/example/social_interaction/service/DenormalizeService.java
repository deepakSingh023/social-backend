package com.example.social_interaction.service;


import com.example.social_interaction.dto.DenormalizeDto;
import com.example.social_interaction.entity.FollowRequest;
import com.example.social_interaction.entity.Follower;
import com.example.social_interaction.entity.FriendRequest;
import com.example.social_interaction.entity.Friends;
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


    private void denormalizeFollower(DenormalizeDto data){

        Query query = Query.query(Criteria.where("userId").is(data.userId()));
        Update update = new Update().set("userAvatar",data.avatar());
        mongoTemplate.updateMulti(query,update, Follower.class);

        Query query2 = Query.query(Criteria.where("followedId").is(data.userId()));
        Update update2 = new Update().set("followedAvatar",data.avatar());
        mongoTemplate.updateMulti(query2,update2, Follower.class);
    }



    private void denormalizeFriends(DenormalizeDto data){

        Query query = Query.query(Criteria.where("senderId").is(data.userId()));
        Update update = new Update().set("senderAvatar",data.avatar());
        mongoTemplate.updateMulti(query,update, Friends.class);

        Query query2 = Query.query(Criteria.where("receiverId").is(data.userId()));
        Update update2 = new Update().set("receiverAvatar",data.avatar());
        mongoTemplate.updateMulti(query2,update2, Friends.class);
    }

    private void denormalizeFriendRequest(DenormalizeDto data){

        Query query = Query.query(Criteria.where("senderId").is(data.userId()));
        Update update = new Update().set("senderAvatar",data.avatar());
        mongoTemplate.updateMulti(query,update, FriendRequest.class);

        Query query2 = Query.query(Criteria.where("receiverId").is(data.userId()));
        Update update2 = new Update().set("receiverAvatar",data.avatar());
        mongoTemplate.updateMulti(query2,update2, FriendRequest.class);
    }

    private void denormalizeFollowerRequest(DenormalizeDto data){

        Query query = Query.query(Criteria.where("userId").is(data.userId()));
        Update update = new Update().set("userAvatar",data.avatar());
        mongoTemplate.updateMulti(query,update, FollowRequest.class);

        Query query2 = Query.query(Criteria.where("followedId").is(data.userId()));
        Update update2 = new Update().set("followedAvatar",data.avatar());
        mongoTemplate.updateMulti(query2,update2, FollowRequest.class);
    }

    @Async("denormalizeExecutor")
    public void denormalizeAll(DenormalizeDto data) {
        denormalizeFollower(data);
        denormalizeFriends(data);
        denormalizeFriendRequest(data);
        denormalizeFollowerRequest(data);
    }
}
