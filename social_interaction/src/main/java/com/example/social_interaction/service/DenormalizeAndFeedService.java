package com.example.social_interaction.service;


import com.example.social_interaction.dto.InteractionDto;
import com.example.social_interaction.dto.UpdateCounter;
import com.example.social_interaction.tasks.CounterClient;
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


    @Async("workerThread")
    public void worker(UpdateCounter data1, UpdateCounter data2, InteractionDto data3, InteractionDto data4){

        try {
            feedWorker.createFeedWorker(data3);
            feedWorker.createFeedWorker(data4);
        } catch (Exception e) {
            log.error("Failed to create feed outbox events", e);
            return;
        }

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





    }


    @Async("workerThread")
    public void followerWorker(UpdateCounter data1, UpdateCounter data2, InteractionDto data3){

        try {
            feedWorker.createFeedWorker(data3);
        } catch (Exception e) {
            log.error("Failed to create feed outbox events", e);
            return;
        }



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



    }
}
