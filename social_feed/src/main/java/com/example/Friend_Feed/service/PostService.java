package com.example.Friend_Feed.service;

import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.FeedResponse;
import com.example.Friend_Feed.dto.InteractionDto;
import com.example.Friend_Feed.entity.Post;
import java.time.Instant;
import java.util.List;

public interface PostService {

    void createFeed(CreateFeed data);

    void createInteractionFeed(InteractionDto data);

    FeedResponse getFeeds(String userId, String cursor , String cursorId);

    void deleteFeeds(String authorId, String feedOwnerId);

    void deleteFeedPost(String postId);
}
