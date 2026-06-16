package com.example.social_interaction.tasks;

import com.example.social_interaction.dto.InternalProfile;
import com.example.social_interaction.dto.ProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "profile" , url = "${profile.uri}")
public interface ProfileClient {


    @GetMapping("/api/profiles/get/profile-stuff/{userId}")
    InternalProfile getInternalData(
            @RequestHeader("X-SECRET-TOKEN") String token,
            @PathVariable String userId
    );

    @PostMapping("/api/profiles/get/basic")
     Map<String, ProfileDto> getProfiles(
            @RequestBody List<String> ids,
            @RequestHeader("X-SECRET-TOKEN") String token
    );

}

