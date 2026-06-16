package com.example.social_interaction.repository;

import com.example.social_interaction.entity.FollowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FollowRequestRepository
        extends MongoRepository<FollowRequest, String> {

    Page<FollowRequest> findByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndFollowedId(String userId, String followedId);
}
