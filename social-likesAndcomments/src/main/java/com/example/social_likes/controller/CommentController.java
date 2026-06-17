package com.example.social_likes.controller;


import com.example.social_likes.dto.CommentResponseDTO;
import com.example.social_likes.dto.CreateCommentDTO;
import com.example.social_likes.enums.LikeTargetType;
import com.example.social_likes.service.CommentsService;
import com.example.social_likes.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentsService commentsService;

    // CREATE COMMENT / REPLY
    @PostMapping("/comment")
    public ResponseEntity<CommentResponseDTO> create(
            @RequestBody CreateCommentDTO data,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commentsService.createComment(data, userId));
    }

    // DELETE COMMENT
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable String commentId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        commentsService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // GET COMMENTS BY POST (TOP LEVEL)
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getPostComments(
            @PathVariable String postId,
            @RequestParam String cursor,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        return ResponseEntity.ok(
                commentsService.getCommentsByPost(postId,userId,cursor)
        );
    }

    // GET REPLIES (NESTED)
    @GetMapping("/replies/{parentCommentId}")
    public ResponseEntity<List<CommentResponseDTO>> getReplies(
            @PathVariable String parentCommentId,
            @RequestParam String cursor,
            Authentication authentication
    ) {

        String userId = authentication.getName();
        return ResponseEntity.ok(
                commentsService.getReplies(parentCommentId,userId,cursor)
        );
    }
}
