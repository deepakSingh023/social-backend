package com.example.social_reel.util;

import com.example.social_reel.dto.InternalProfile;
import com.example.social_reel.dto.ReelUpdate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "profile" , url = "${profile.uri}")
public interface ProfileClient {


    @GetMapping("/api/profiles/get/profile-stuff/{userId}")
    InternalProfile getInternalData(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @PathVariable String userId
    );


    @PutMapping("/api/controller/counter/reel-number")
    void updateReelCounter(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestBody ReelUpdate data
            );

}
