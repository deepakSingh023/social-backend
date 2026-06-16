package com.example.social_likes.service;


import com.example.social_likes.dto.IncrementDecDto;
import com.example.social_likes.dto.LikeRequestDTO;
import com.example.social_likes.dto.LikeResponseDTO;
import com.example.social_likes.entity.Likes;
import com.example.social_likes.enums.ImpressionType;
import com.example.social_likes.enums.LikeTargetType;
import com.example.social_likes.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final DenormalizeService denormalizeService;

    @Override
    public LikeResponseDTO createLike(LikeRequestDTO data, String userId) {

        // ❌ invalid input → exception
        if (data.getTargetId() == null || data.getTargetType() == null) {
            throw new IllegalArgumentException("TargetId and TargetType are required");
        }

        // ✅ already liked → NORMAL behavior
        boolean alreadyLiked =
                likesRepository.existsByUserIdAndTargetIdAndTargetType(
                        userId,
                        data.getTargetId(),
                        data.getTargetType()
                );

        if (alreadyLiked) {
            return LikeResponseDTO.builder()
                    .targetId(data.getTargetId())
                    .targetType(data.getTargetType())
                    .liked(true)
                    .build();
        }

        // ✅ create like
        Likes like = Likes.builder()
                .userId(userId)
                .targetId(data.getTargetId())
                .targetType(data.getTargetType())
                .createdAt(Instant.now())
                .build();

        likesRepository.save(like);

        denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(data.getTargetId(), ImpressionType.LIKE,+1),data.getTargetType(),userId);





        return LikeResponseDTO.builder()
                .targetId(data.getTargetId())
                .targetType(data.getTargetType())
                .liked(true)
                .build();
    }

    @Override
    public LikeResponseDTO removeLike(LikeRequestDTO data, String userId) {

        if (data.getTargetId() == null || data.getTargetType() == null) {
            throw new IllegalArgumentException("TargetId and TargetType are required");
        }

        likesRepository.deleteByUserIdAndTargetIdAndTargetType(
                userId,
                data.getTargetId(),
                data.getTargetType()
        );

        denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(data.getTargetId(), ImpressionType.LIKE,-1),data.getTargetType(),userId);

        return LikeResponseDTO.builder()
                .targetId(data.getTargetId())
                .targetType(data.getTargetType())
                .liked(false)
                .build();
    }

    @Override
    public List<LikeResponseDTO> getAllLikesByUser(String userId) {

        List<Likes> likes = likesRepository.findAllByUserId(userId);

        return likes.stream()
                .map(like -> LikeResponseDTO.builder()
                        .targetId(like.getTargetId())
                        .targetType(like.getTargetType())
                        .liked(true)
                        .build())
                .toList();
    }

    @Override
    public long getLikesCount(String postId) {
        return likesRepository.countByTargetIdAndTargetType(
                postId,
                LikeTargetType.POST
        );
    }


    @Override
    public Map<String,Boolean> likedList(String userId, List<String> targetIds){

        List<Likes> likes = likesRepository.findByUserIdAndTargetIdIn(userId,targetIds);

        Set<String> likeIDs = likes.stream().map(
                Likes::getTargetId
        ).collect(Collectors.toSet());

        HashMap<String,Boolean> response = new HashMap<>();

        for(String postId:targetIds){

            response.put(postId,likeIDs.contains(postId));
        }
        return response;

    }


    @Override
    public boolean isLiked(String userId, String postId){

        return  likesRepository.existsByUserIdAndTargetId(userId,postId);
    }

}




