package com.example.social_reel.repository;


import com.example.social_reel.entity.Reel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface ReelRepository extends MongoRepository<Reel, String> {

    List<Reel> findByUserId(String userId);

    List<Reel> findBySemanticTagsInAndCreatedAtLessThanOrderByCreatedAtDesc(
            Set<String> tags,
            Instant cursor,
            Pageable pageable
    );

    List<Reel> findByCreatedAtLessThanOrderByCreatedAtDesc(
            Instant cursor,
            Pageable pageable
    );

    List<Reel> findByCreatedAtLessThanOrderByPopularityScoreDesc(
            Instant cursor,
            Pageable pageable
    );

    List<Reel> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Reel> findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(String userId, Instant cursorCreatedAt,
                                                                    Pageable pageable);


}
