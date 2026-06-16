package com.example.social_post.util;

import com.example.social_post.dto.InternalProfile;
import com.example.social_post.dto.ReelUpdate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "profile" , url = "${profile.uri}")
public interface ProfileClient {


    @GetMapping("/api/profiles/get/profile-stuff/{userId}")
        InternalProfile getInternalData(
                @RequestHeader("X-SECRET-TOKEN") String token,
                @PathVariable String userId
    );

    @PutMapping("/api/controller/counter/post-number")
    void updatePostCounter(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @RequestBody ReelUpdate data
    );


}
