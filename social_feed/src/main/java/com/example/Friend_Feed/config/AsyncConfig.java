package com.example.Friend_Feed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
public class AsyncConfig {

    @Bean("createFeed")
    public Executor FeedCreation(){
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



    @Bean("createPostFeed")
    public Executor PostFeedCreation(){

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();
        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-createPostFeed");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();
        return thread;
    }

    @Bean("deleteFeed")
    public Executor deleteFeed(){
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
