package com.example.Social.profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableAsync
@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class SocialProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialProfileApplication.class, args);
	}

}
