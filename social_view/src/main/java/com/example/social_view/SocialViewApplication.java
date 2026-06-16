package com.example.social_view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableFeignClients
@EnableScheduling
@SpringBootApplication
public class SocialViewApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialViewApplication.class, args);
	}

}
