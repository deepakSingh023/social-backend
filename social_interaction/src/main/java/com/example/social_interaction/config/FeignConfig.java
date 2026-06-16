package com.example.social_interaction.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100,      // initial interval (ms)
                1000,     // max interval (ms)
                3         // max attempts
        );
    }
}