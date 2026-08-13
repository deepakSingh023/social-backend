package com.example.Friend_Feed.service;


import com.example.Friend_Feed.dto.CreateFeed;
import com.example.Friend_Feed.dto.InteractionFeedEvent;
import com.example.Friend_Feed.dto.PostFeedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedListenerService {

    private final FanOutOnWriteFeedService fanOutOnWriteFeedService;

    @KafkaListener(topics = "post-feed-events")
    public void handlePostEvent(PostFeedEvent event) {

        fanOutOnWriteFeedService.createFeed(new CreateFeed(event.authorId(), event.postId()));

    }

    @KafkaListener(topics = "interction-feed-events")
    public void handleInteractionEvent(InteractionFeedEvent event) {
        feedService.createFromPost(
                event.postId(),
                event.authorId()
        );
    }
}
