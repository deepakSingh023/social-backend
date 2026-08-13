package com.example.Friend_Feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableKafka
@SpringBootApplication
@EnableAsync
@EnableFeignClients
@EnableScheduling
public class FriendFeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(FriendFeedApplication.class, args);
	}

}
