package com.example.social_fetchReels.util;


import com.example.social_fetchReels.entity.UserInterest;
import org.apache.catalina.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name="interest",url="${interest.uri}")
public interface InterestClient {


    @PostMapping("/api/interests/getInterest")
    UserInterest getInterest(
            @RequestBody String userId,
            @RequestHeader("X-SECRET-TOKEN") String token

    );
}
