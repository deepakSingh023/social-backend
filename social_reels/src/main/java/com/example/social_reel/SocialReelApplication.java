package com.example.social_reel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableFeignClients
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SocialReelApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialReelApplication.class, args);
	}

}
