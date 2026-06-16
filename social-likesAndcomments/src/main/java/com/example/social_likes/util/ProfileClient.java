package com.example.social_likes.util;

import com.example.social_likes.dto.InternalProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile" , url = "${profile.uri}")
public interface ProfileClient {


    @GetMapping("/api/profiles/get/profile-stuff/{userId}")
    InternalProfile getInternalData(
            @PathVariable String userId,
            @RequestHeader("X-SECRET-TOKEN") String token
    );

}
