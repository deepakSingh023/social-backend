package com.example.social_interaction.controller;


import com.example.social_interaction.dto.CheckInteraction;
import com.example.social_interaction.dto.InteractionResponse;
import com.example.social_interaction.service.CheckInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interaction")
public class CheckController {

    private final CheckInteractionService checkInteractionService;

    @PostMapping("/check")
    public ResponseEntity<InteractionResponse> checkInteraction(
            @RequestBody CheckInteraction data
            ){

        return ResponseEntity.ok(checkInteractionService.checkInteraction(data));

    }


}
