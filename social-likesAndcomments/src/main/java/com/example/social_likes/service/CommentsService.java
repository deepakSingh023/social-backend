package com.example.social_likes.service;

import com.example.social_likes.dto.CommentResponseDTO;
import com.example.social_likes.dto.CreateCommentDTO;
import com.example.social_likes.enums.LikeTargetType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentsService {

    CommentResponseDTO createComment(CreateCommentDTO data, String userId);

    void deleteComment(String commentId, String userId);

    List<CommentResponseDTO> getCommentsByPost(String postId,String userId, String cursor);

    List<CommentResponseDTO> getReplies(String parentCommentId,String userId,  String cursor);
}
