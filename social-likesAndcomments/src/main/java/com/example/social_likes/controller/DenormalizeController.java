package com.example.social_likes.controller;


import com.example.social_likes.dto.DenormalizeDto;
import com.example.social_likes.service.DenormalizeService;
import com.example.social_likes.service.LikeAndCommentCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments/denormalize")
public class DenormalizeController {

    private final LikeAndCommentCleanupService likeAndCommentCleanupService;


    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> deleteLikesAndComments(
            @RequestParam String targetId
    ){

        likeAndCommentCleanupService.deleteLikesAndComment(targetId);

        return ResponseEntity.accepted().build();
    }

}
