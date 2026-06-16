package com.example.social_post.controller;


import com.example.social_post.dto.IncrementDecDto;
import com.example.social_post.service.LikesAndCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class IncAndDecController {

    private final LikesAndCommentService likesAndCommentService;


    @PutMapping("/like/inc")
    public ResponseEntity<Void> likeInc(
            @RequestBody IncrementDecDto data
            ){

        likesAndCommentService.incrementDecrement(data);

        return ResponseEntity.accepted().build();


    }

}
