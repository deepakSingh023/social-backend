package com.example.auth_social.tasks;


import com.example.auth_social.dto.CreateProfile;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@org.springframework.cloud.openfeign.FeignClient(name="profile-creation" ,url="${profile.service.url}")
public interface FeignClient {



    @PostMapping("/api/profiles/create")
    void createProfile(
            @RequestBody CreateProfile data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );

}

