package com.example.social_post.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {


    @Bean("denormalize")
    public Executor denormalizeExecutor(){

        ThreadPoolTaskExecutor  thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-denormalize");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();
        return thread;
    }


    @Bean("feedCreate")
    public Executor createTheFeed(){

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-createFeed");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();
        return thread;

    }

    @Bean("feedDelete")
    public Executor deleteTheFeed(){

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-deleteFeed");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();
        return thread;

    }
}
