package com.example.social_likes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class SocialLikesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialLikesApplication.class, args);
	}

}
