package com.example.social_likes.controller;

import com.example.social_likes.dto.LikeRequestDTO;
import com.example.social_likes.dto.LikeResponseDTO;
import com.example.social_likes.service.LikesService;
import com.example.social_likes.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    @PostMapping("/like/create")
    public ResponseEntity<LikeResponseDTO> like(
            @RequestBody LikeRequestDTO request,
            Authentication authentication
    ) {
        String userId = SecurityUtil.getCurrentUserId(authentication);
        return ResponseEntity.ok(likesService.createLike(request, userId));
    }

    @DeleteMapping("/like/unlike")
    public ResponseEntity<LikeResponseDTO> unlike(
            @RequestBody LikeRequestDTO request,
            Authentication authentication
    ) {
        String userId = SecurityUtil.getCurrentUserId(authentication);
        return ResponseEntity.ok(likesService.removeLike(request, userId));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Long> getLikesCount(@PathVariable String postId) {
        return ResponseEntity.ok(likesService.getLikesCount(postId));
    }

    @PostMapping("/likes/isLiked")
    public ResponseEntity<Map<String,Boolean>> getLiked(
            @RequestParam String userId,
            @RequestBody List<String> postIds
    ){

        Map<String,Boolean> res = likesService.likedList(userId,postIds);

        return ResponseEntity.ok(res);

    }


    @GetMapping("/likes/isLikedIndividual")
    public ResponseEntity<Boolean> getIndividualLiked(
            @RequestParam String userId,
            @RequestParam String postId
    ){

        boolean res = likesService.isLiked(userId,postId);

        return ResponseEntity.ok(res);

    }
}
