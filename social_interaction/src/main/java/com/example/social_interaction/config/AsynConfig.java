package com.example.social_interaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsynConfig {

    @Bean("denormalizeExecutor")
    public Executor denormalizeThread() {

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(200);
        thread.setThreadNamePrefix("-denormalize");

        thread.setTaskDecorator(
                new ContextPropagatingTaskDecorator()
        );

        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        thread.initialize();
        return thread;
    }


    @Bean("conversationUpdate")
    public Executor conversationUpdate() {

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-conversation");

        thread.setTaskDecorator(
                new ContextPropagatingTaskDecorator()
        );

        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        thread.initialize();
        return thread;
    }


    @Bean("workerThread")
    public Executor workerThread() {

        ThreadPoolTaskExecutor thread = new ThreadPoolTaskExecutor();

        thread.setCorePoolSize(10);
        thread.setMaxPoolSize(20);
        thread.setQueueCapacity(100);
        thread.setThreadNamePrefix("-worker");

        thread.setTaskDecorator(
                new ContextPropagatingTaskDecorator()
        );

        thread.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        thread.initialize();
        return thread;
    }
}
