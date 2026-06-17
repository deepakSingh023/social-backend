package com.example.social_likes.repository;

import com.example.social_likes.entity.Likes;
import com.example.social_likes.enums.LikeTargetType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesRepository extends MongoRepository<Likes, String> {

    boolean existsByUserIdAndTargetIdAndTargetType(
            String userId,
            String targetId,
            LikeTargetType targetType
    );

    void deleteByUserIdAndTargetIdAndTargetType(
            String userId,
            String targetId,
            LikeTargetType targetType
    );

    List<Likes> findAllByUserId(String userId);

    long countByTargetIdAndTargetType(String targetId, LikeTargetType targetType);

    List<Likes> findByUserIdAndTargetIdIn(String userId, List<String> likes);

    boolean existsByUserIdAndTargetId(String userId,String postId);

    void deleteAllByTargetId(String targetId);

}
