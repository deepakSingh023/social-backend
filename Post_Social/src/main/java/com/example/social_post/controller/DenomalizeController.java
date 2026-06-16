package com.example.social_post.controller;


import com.example.social_post.dto.DenormalizeDto;
import com.example.social_post.service.DenormalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/denormalize")
public class DenomalizeController {

    private final DenormalizeService denormalizeService;


    @PutMapping("/avatar")
    public ResponseEntity<Void> denormalize(
            @RequestBody DenormalizeDto data
            ){

        denormalizeService.avatarDenormalization(data.userId(), data.avatar());

        return ResponseEntity.accepted().build();

    }
}
