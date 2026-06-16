package com.example.social_interaction.service;

import com.example.social_interaction.entity.Feed;
import com.example.social_interaction.repository.FeedRepository;
import com.example.social_interaction.repository.FriendRepository;
import com.example.social_interaction.repository.RelationRepository;
import com.example.social_interaction.tasks.PostClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;


@RequiredArgsConstructor
@Service
public class InteractonService {


    private final FeedRepository feedRepository;

    private final FriendRepository friendRepository;

    private final RelationRepository relationRepository;

    private final PostClient postClient;


    @Value("${service.secret}")
    private String token;



    @Async
    public void createInteraction(String authorId, String recipientId){

        if(feedRepository.existsByAuthorIdOrRecipientUserId(authorId,recipientId)){
            return;
        }

        Feed feed = Feed.builder()
                .authorId(authorId)
                .recipientUserId(recipientId)
                .createdAt(Instant.now())
                .build();


        feedRepository.save(feed);
    }

    @Async
    public void deleteInteraction(String authorId, String recipientId){

        boolean friendshipExists =
                friendRepository.existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                        authorId,
                        recipientId,
                        recipientId,
                        authorId
                );

        boolean followExists =
                relationRepository.existsByUserIdAndFollowedId(
                        recipientId,
                        authorId
                );

        // if NO relation left -> remove interaction
        if(!friendshipExists && !followExists){

            feedRepository.deleteByAuthorIdAndRecipientUserId(
                    authorId,
                    recipientId
            );

            postClient.deleteFeed(token,recipientId,authorId);
        }
    }
}
