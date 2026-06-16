package com.example.auth_social.services;


import com.example.auth_social.dto.CreateProfile;
import com.example.auth_social.tasks.FeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class AsyncService {

    private final FeignClient feignClient;

    private final Logger log = LoggerFactory.getLogger(AsyncService.class);



    @Retry(name = "importantApi")
    @CircuitBreaker(
            name = "importantApi",
            fallbackMethod = "fallback"
    )
    @Async("profileExecutor")
    public void createProfiles(CreateProfile data, String secret){
            feignClient.createProfile(data, secret);
    }

    public void fallback(
            CreateProfile data,
            String secret,
            Throwable ex
    ) {

        log.error(
                "Profile creation failed after retries for user {}",
                data.userId(),
                ex
        );
    }
}
