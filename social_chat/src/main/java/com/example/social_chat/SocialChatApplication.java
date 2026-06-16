package com.example.social_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication

@EnableScheduling
public class SocialChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialChatApplication.class, args);
	}

}
