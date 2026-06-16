package com.example.social_post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableFeignClients
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SocialPostApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialPostApplication.class, args);
	}

}
