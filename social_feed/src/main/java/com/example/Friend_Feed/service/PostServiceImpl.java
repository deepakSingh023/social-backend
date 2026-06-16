package com.example.Friend_Feed.service;

import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.FeedResponse;
import com.example.Friend_Feed.dto.InteractionDto;
import com.example.Friend_Feed.dto.PostResponse;
import com.example.Friend_Feed.entity.Feed;
import com.example.Friend_Feed.entity.Post;
import com.example.Friend_Feed.filter.JwtAuthenticationFilter;
import com.example.Friend_Feed.repository.FeedRepository;
import com.example.Friend_Feed.utils.LikesClient;
import com.example.Friend_Feed.utils.PostClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final FeedRepository feedRepository;

    private final FanOutOnWriteFeedService fanOutOnWriteFeedService;

    private final PostClient postClient;

     private final LikesClient likesClient;

     private final static int INT_SIZE=10;

    private final static Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    @Value("${service.secret}")
    private String token;

    @Override
    public void createFeed(CreateFeed data){
        fanOutOnWriteFeedService.createFeed(data);
    }


    @Override
    public void createInteractionFeed(InteractionDto data){
        fanOutOnWriteFeedService.createFeedForInteraction(data);
    }

    @Override
    public FeedResponse getFeeds(String userId, String cursor, String cursorId){

        log.info("service reached");


        List<Feed> feeds;



        if(cursor== null && cursorId==null){
            feeds = feedRepository.findTop11ByFeedOwnerIdOrderByCreatedAtDescIdDesc(userId,PageRequest.of(0,INT_SIZE+1));
        }else{

            Instant convertedCursor = Instant.parse(cursor);

            feeds = feedRepository.getFeed(
                    userId,
                    convertedCursor,
                    cursorId,
                    PageRequest.of(0,INT_SIZE+1)
            );
        }

        log.info("feed foud");

        boolean hasMore = feeds.size()>10;

        if(hasMore){
            feeds = feeds.subList(0,10);

        }

        List<String> postIds = feeds.stream()
                .map(Feed::getPostId)
                .toList();


        List<Post> posts = postClient.getFeedPosts(token,postIds);





        Map<String,Boolean> likes = Collections.emptyMap();

        try{
            likes = likesClient.getLiked(userId,postIds,token);
        }catch (Exception e){
            log.error("likes service if offline or is unreachable ",e);
        }

        Map<String,Boolean> likeData = likes;

        List<PostResponse> res = posts.stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getUserId(),
                        post.getAvatar(),
                        post.getUsername(),
                        post.getImageUrls(),
                        post.getVideoUrl(),
                        post.getCaption(),
                        post.getSongUrl(),
                        post.getSongName(),
                        post.getArtistName(),
                        post.getTags(),
                        post.isPrivate(),
                        post.getCreatedAt(),
                        post.getComments(),
                        post.getLikes(),
                        likeData.getOrDefault(post.getId(), false)
                )).toList();


        if(posts.isEmpty()){
            return new FeedResponse(res,null,null,hasMore);
        }


        Feed last = feeds.get(feeds.size() - 1);

        log.info("respnse send serice");

        return new FeedResponse(
                res,
                last.getCreatedAt(),
                last.getId(),
                hasMore
        );

    }

    public void deleteFeeds(String authorId, String feedOwnerId){

        fanOutOnWriteFeedService.deleteFeed(feedOwnerId,authorId);
    }

    public void deleteFeedPost(String postId){

        fanOutOnWriteFeedService.deleteFeedPost(postId);
    }

}