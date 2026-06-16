package com.example.social_interaction.service;


import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.dto.UpdateCounter;
import com.example.social_interaction.tasks.CounterClient;
import com.example.social_interaction.tasks.PostClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class DenormalizeAndFeedService {

    private final static Logger log = LoggerFactory.getLogger(DenormalizeAndFeedService.class);

    private final CounterClient counterClient;

    private final FeedWorker feedWorker;

    @Value("${service.secret}")
    private String secret;


    @Async
    public void worker(UpdateCounter data1, UpdateCounter data2, InteractionDto data3, InteractionDto data4){

        try {
            counterClient.denormalize(data1,secret);
        } catch(Exception e) {
            log.error("counter denormalization failed for  user={}",data1.userId(),e);
        }

        try {
            counterClient.denormalize(data2,secret);
        } catch(Exception e) {
            log.error("counter denormalization failed for user={}",data2.userId(),e);
        }


        feedWorker.createFeedWorker(data3,secret);

        feedWorker.createFeedWorker(data4,secret);


    }


    @Async
    public void followerWorker(UpdateCounter data1, UpdateCounter data2, InteractionDto data3){

        try {
            counterClient.denormalize(data1,secret);
        } catch(Exception e) {
            log.error("counter denormalization failed for  user1={}",data1.userId(),e);
        }

        try {
            counterClient.denormalize(data2,secret);
        } catch(Exception e) {
            log.error("counter denormalization failed for user2={}",data2.userId(),e);
        }

        feedWorker.createFeedWorker(data3,secret);

    }
}
