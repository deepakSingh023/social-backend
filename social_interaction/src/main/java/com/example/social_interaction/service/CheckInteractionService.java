package com.example.social_interaction.service;


import com.example.social_interaction.dto.CheckInteraction;
import com.example.social_interaction.dto.InteractionResponse;
import com.example.social_interaction.repository.FriendRepository;
import com.example.social_interaction.repository.RelationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckInteractionService {


    private final FriendRepository friendRepository;

    private final RelationRepository relationRepository;

    private static final Logger log = LoggerFactory.getLogger(CheckInteractionService.class);


    public InteractionResponse checkInteraction(CheckInteraction data){

        log.info("the service is working fine");

        long start = System.currentTimeMillis();

        boolean isFollow = relationRepository.existsByUserIdAndFollowedId(data.userId(), data.otherUserId());

        boolean isFriend = friendRepository.existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(data.userId(), data.otherUserId(), data.otherUserId(), data.userId());

        long end = System.currentTimeMillis();

        log.info("latency = {} info = the service is working ",end-start);
        return new InteractionResponse(isFollow,isFriend);

    }
}
