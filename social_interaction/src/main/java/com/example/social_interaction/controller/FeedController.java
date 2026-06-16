package com.example.social_interaction.controller;


import com.example.social_interaction.dto.RecipientPage;
import com.example.social_interaction.service.FeedInteractionFetch;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interactions")
public class FeedController {


    private final FeedInteractionFetch feedInteractionFetch;



    @GetMapping("/getInteractions")
    public ResponseEntity<RecipientPage> getInteractionFeed(
            @RequestParam String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam int size
    ){

        RecipientPage data = feedInteractionFetch.getInteractions(userId,size,cursor);

        return ResponseEntity.ok(data);

    }
}
