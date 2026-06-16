package com.example.Friend_Feed.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    @Value("${ping.url}")
    private String url ;

    private final RestTemplate restTemplate = new RestTemplate();

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5 min
    public void pingSelf() {
        try {
            restTemplate.getForObject(url, String.class);
            System.out.println("Keep-alive ping sent to " + url);
        } catch (Exception e) {
            System.err.println("Keep-alive ping failed: " + e.getMessage());
        }
    }
}
