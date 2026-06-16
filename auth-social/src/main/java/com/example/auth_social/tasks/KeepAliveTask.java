package com.example.auth_social.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    private static final String PING_URL = "http://auth-service:8080/api/health";

    private final RestTemplate restTemplate = new RestTemplate();

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5 min
    public void pingSelf() {
        try {
            restTemplate.getForObject(PING_URL, String.class);
            System.out.println("Keep-alive ping sent to " + PING_URL);
        } catch (Exception e) {
            System.err.println("Keep-alive ping failed: " + e.getMessage());
        }
    }
}
