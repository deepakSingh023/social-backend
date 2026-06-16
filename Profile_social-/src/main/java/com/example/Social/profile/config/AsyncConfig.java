package com.example.Social.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {


    @Bean(name="denormalize")
    public Executor ProfileExecutor(){

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-denormalizeProfile");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();

        return thread;
    }

    @Bean(name="update-profile")
    public Executor ProfileNumberExecutor(){

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();
        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-updateNumbers");
        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        thread.initialize();

        return thread;
    }
}
