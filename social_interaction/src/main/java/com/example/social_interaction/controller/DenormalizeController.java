package com.example.social_interaction.controller;


import com.example.social_interaction.dto.DenormalizeDto;
import com.example.social_interaction.service.DenormalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interaction")
public class DenormalizeController {

    private final DenormalizeService denormalizeService;

    @PutMapping("/denormalize")
    public ResponseEntity<Void> denormalizeIneraction(
            @RequestBody DenormalizeDto data
            ){

        denormalizeService.denormalizeAll(data);
        return ResponseEntity.accepted().build();
    }
}
