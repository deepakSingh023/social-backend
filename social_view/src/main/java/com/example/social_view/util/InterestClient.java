package com.example.social_view.util;


import com.example.social_view.dto.InterestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "interest-service",url = "${interest.uri}")
public interface InterestClient {

    @PostMapping("/api/interests/update")
    void sendInterest(
            @RequestBody InterestDto event,
            @RequestHeader("X-SECRET-TOKEN") String token);

}