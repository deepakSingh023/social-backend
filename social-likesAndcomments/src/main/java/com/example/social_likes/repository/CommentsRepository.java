package com.example.social_likes.repository;

import com.example.social_likes.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface CommentsRepository extends MongoRepository<Comments, String> {

    List<Comments> findByPostIdAndParentCommentIdIsNullAndCreatedAtLessThan(
            String postId,
            Instant cursor,
            Pageable pageable
    );

    List<Comments> findByParentCommentIdAndCreatedAtLessThan(
            String parentCommentId,
            Instant cursor,
            Pageable pageable

    );

    List<Comments> findByParentCommentId(String parentCommentId,Pageable pageable);

    List<Comments> findByPostIdAndParentCommentIdIsNull(String postId, Pageable pageable);

    List<Comments> findByUserId(String userId);
}
