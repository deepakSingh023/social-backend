package com.example.social_likes.service;


import com.example.social_likes.dto.IncrementDecDto;
import com.example.social_likes.entity.Comments;
import com.example.social_likes.enums.ImpressionType;
import com.example.social_likes.enums.LikeTargetType;
import com.example.social_likes.util.PostClient;
import com.example.social_likes.util.ReelClient;
import com.example.social_likes.util.ViewClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class DenormalizeService {

    private final MongoTemplate mongoTemplate;

    private final PostClient postClient;

    private final ReelClient reelClient;

    private final ViewClient viewClient;


    @Value("${service.secret}")
    private  String token;



    @Async
    public void denormalizeLikeAndCommentCount(IncrementDecDto data , LikeTargetType imp, String userId){

        switch (imp){
            case POST -> postClient.likeInc(token,data);
            case REEL -> {
                reelClient.likeInc(data,token);

                if(data.num()>0){
                    viewClient.createLikeInterest(data.postId(),userId,token );
                }

            }
            case COMMENT -> denormalizeCommentLike(data);
            case COMMENT_REPLY -> denormalizeCommentReply(data);
            default ->
                    throw new IllegalArgumentException("wrong type for the impression: " + data.type());

        }
    }

    public void denormalizeCommentLike(IncrementDecDto data){

        Query query = new Query();

        query.addCriteria(Criteria.where("_id").is(data.postId()));

        Update update = new Update();

        update.inc(data.type().getField(), data.num());

        mongoTemplate.updateFirst(query,update, Comments.class);


    }

    public void denormalizeCommentReply(IncrementDecDto data){

        Query query = new Query();

        query.addCriteria(Criteria.where("_id").is(data.postId()));

        Update update = new Update();

        update.inc(data.type().getField(), data.num());

        mongoTemplate.updateFirst(query,update, Comments.class);



    }

    @Async
    public void denormalizeCommentAvatar(String avatar , String userId){

        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));

        Update update = new Update();

        update.set("userAvatar",avatar);

        mongoTemplate.updateMulti(query,update,Comments.class);

    }
}
