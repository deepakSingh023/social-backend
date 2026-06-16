package com.example.social_likes.controller;


import com.example.social_likes.dto.DenormalizeDto;
import com.example.social_likes.service.DenormalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments/denormalize")
public class DenormalizeController {

    private final DenormalizeService denormalizeService;

    @PutMapping("/update")
    public ResponseEntity<Void> denormalizeImage(
            @RequestBody DenormalizeDto data
            ){
        denormalizeService.denormalizeCommentAvatar(data.avatar(),data.userId());
        return ResponseEntity.accepted().build();
    }

}
