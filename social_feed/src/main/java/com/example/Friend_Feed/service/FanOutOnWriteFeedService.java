package com.example.Friend_Feed.service;


import com.example.Friend_Feed.aspect.LogAspect;
import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.InteractionDto;
import com.example.Friend_Feed.dto.RecipientPage;
import com.example.Friend_Feed.dto.RecipientsPosts;
import com.example.Friend_Feed.entity.Feed;
import com.example.Friend_Feed.repository.FeedRepository;
import com.example.Friend_Feed.utils.InteractionClient;
import com.example.Friend_Feed.utils.PostClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FanOutOnWriteFeedService {

    private final FeedRepository feedRepository;

    private final MongoTemplate mongoTemplate;

    private final FeedDataFetch feedDataFetch;

    public static final Logger log  = LoggerFactory.getLogger(FanOutOnWriteFeedService.class);

    @Value("${service.secret}")
    private String token;


    //used to create feed on creation of post or a new interaction following a fan out on write architecture

    @Async("createFeed")
    public void createFeed(CreateFeed data){

        String authorId = data.userId();
        String postId = data.postId();
        int size = 100;
        String cursor = null;


        do{

            log.info(
                    "Fetching followers author={} cursor={}",
                    authorId,
                    cursor
            );

            RecipientPage page =  feedDataFetch.getInteractionData(data.userId(),cursor,size,token);

            List<Feed> feeds = new ArrayList<>();



            for (String recipientId : page.userIds()) {
                Feed feed = Feed.builder()
                        .feedOwnerId(recipientId)
                        .authorId(authorId)
                        .postId(postId)
                        .createdAt(Instant.now())
                        .build();

                feeds.add(feed);
            }

            if (!feeds.isEmpty()) {
                feedRepository.saveAll(feeds);
            }

            cursor = page.nextCursor();

        } while (cursor != null);

    }

    @Async("createPostFeed")
    public void createFeedForInteraction(InteractionDto data){

        String authorId = data.authorId();
        String ownerId = data.feedOwnerId();

        int size = 100;
        String cursor = null;

        do {

            RecipientsPosts posts = feedDataFetch.getPostData(token,data.authorId(),size,cursor);

            List<Feed> feedList = new ArrayList<>();

            for(String postId : posts.postId()){

                Feed feed = Feed.builder()
                        .feedOwnerId(ownerId)
                        .authorId(authorId)
                        .postId(postId)
                        .createdAt(Instant.now())
                        .build();

                feedList.add(feed);
            }


            if(!feedList.isEmpty()){
                feedRepository.saveAll(feedList);
            }

            cursor = posts.cursor();

        } while (cursor != null);

    }

    @Async("deleteFeed")
    public void deleteFeed(String feedOwnerId, String authorId){

        Query query = new Query();

        query.addCriteria(
                Criteria.where("authorId").is(authorId)
                        .and("feedOwnerId").is(feedOwnerId)
        );

        mongoTemplate.remove(query, Feed.class);

    }

    @Async("deleteFeed")
    public void deleteFeedPost(String postId){

        Query query = new Query();

        query.addCriteria(
                Criteria.where("postId").is(postId)

        );

        mongoTemplate.remove(query, Feed.class);



    }
}
