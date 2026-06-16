package com.example.social_reel.controller;


import com.example.social_reel.dto.DenormalizeDto;
import com.example.social_reel.dto.ViewDto;
import com.example.social_reel.entity.Reel;
import com.example.social_reel.service.DenormalizeService;
import com.example.social_reel.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reel")
public class DenormalizController {

    private final DenormalizeService denormalizeService;

    private final ViewService viewService;

    @PutMapping("/denormalize")
    public ResponseEntity<Void> reelDenormalize(
            @RequestBody DenormalizeDto data
            ){

        denormalizeService.denormalize(data.userId(), data.avatar());

        return ResponseEntity.accepted().build();

    }


    @PutMapping("/view-update")
    public ResponseEntity<Set<String>> updateView(
            @RequestBody ViewDto data
            ){

        Set<String> tags = viewService.viewUpdate(data);

        return ResponseEntity.ok(tags);
    }
}
