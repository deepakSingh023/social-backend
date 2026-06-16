package com.example.Social.profile.service;


import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.tasks.CommentsClient;
import com.example.Social.profile.tasks.InteractionClient;
import com.example.Social.profile.tasks.PostClient;
import com.example.Social.profile.tasks.ReelClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class DenormalizeService {


    private final R2ImageService r2ImageService;


    private static final Logger log = LoggerFactory.getLogger(DenormalizeService.class);

    @Value("${secret.service}")
    private String secret;

    private final DenormalizeWorker worker;

    @Async("denormalize")
    public void denormalize(DenormalizeDto data){

            worker.denormPost(data,secret);
            worker.denormReel(data,secret);
            worker.denormComment(data,secret);
            worker.denormInteraction(data,secret);

    }



    @Async("denormalize")
    public void deleteOldImageAsync(String oldUrl) {
        try {
            r2ImageService.deleteImage(oldUrl);
        } catch (Exception e) {
            log.warn("delete old image failed oldUrl={}", oldUrl, e);
        }
    }

    public void fallback(
            DenormalizeDto data,
            Throwable ex
    ){
        log.error("profile creation failed after retries for user = {}",
                data.userId(),
                ex);

    }
}
