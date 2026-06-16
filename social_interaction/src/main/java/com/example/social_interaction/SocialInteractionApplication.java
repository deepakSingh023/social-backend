package com.example.social_interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class SocialInteractionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialInteractionApplication.class, args);
	}

}
