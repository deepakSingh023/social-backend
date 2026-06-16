package com.example.social_likes.service;

import com.example.social_likes.dto.CommentResponseDTO;
import com.example.social_likes.dto.CreateCommentDTO;
import com.example.social_likes.dto.LikeRequestDTO;
import com.example.social_likes.dto.LikeResponseDTO;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.List;
import java.util.Map;

public interface LikesService {

    LikeResponseDTO createLike(LikeRequestDTO data, String userId);

    LikeResponseDTO removeLike(LikeRequestDTO data, String userId);

    List<LikeResponseDTO> getAllLikesByUser(String userId);

    long getLikesCount(String postId);

    Map<String,Boolean> likedList(String userId, List<String> postIds);

    boolean isLiked(String userId, String postId);

}
