package com.example.social_reel.controller;


import com.example.social_reel.dto.IncrementDecDto;
import com.example.social_reel.service.IncAndDecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reel")
public class IncAndDecController {

    private final IncAndDecService incAndDecService;


    @PutMapping("/like/inc")
    public ResponseEntity<Void> likeInc(
            @RequestBody IncrementDecDto data
            ){

        incAndDecService.incrementLike(data);

        return ResponseEntity.accepted().build();


    }

}