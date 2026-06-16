package com.example.Friend_Feed.utils;


import com.example.Friend_Feed.dto.RecipientPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="interaction", url="${interaction.uri}")
public interface InteractionClient {

    @GetMapping("/api/interaction/getInteractions")
    RecipientPage getInteractionIds(@RequestParam String userId,
                                    @RequestParam(required = false) String cursor,
                                    @RequestParam int size,
                                    @RequestHeader("X-SECRET-TOKEN") String token
    );

}
