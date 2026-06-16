package com.example.Social.profile.tasks;

import com.example.Social.profile.dto.CheckInteraction;
import com.example.Social.profile.dto.DenormalizeDto;
import com.example.Social.profile.dto.InteractionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="denormalizeInteraction", url="${interaction.uri}")
public interface InteractionClient {

    @PutMapping("/api/interaction/denormalize")
    void denormalize(
            @RequestBody DenormalizeDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );

    @PostMapping("/api/interaction/check")
    InteractionResponse checkInteraction(
            @RequestBody CheckInteraction data,
            @RequestHeader("X-SECRET-TOKEN") String token
            );
}
