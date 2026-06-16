package com.example.social_interaction.repository;

import com.example.social_interaction.entity.Feed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FeedRepository extends MongoRepository<Feed, String> {

    boolean existsByAuthorIdOrRecipientUserId(String authorId, String recipientUserId);

    Optional<Feed> findByAuthorIdAndRecipientUserId(String authorId, String recipientUserId);

    List<Feed> findTop100ByAuthorIdOrderByCreatedAtDescIdDesc(String authorId, Pageable pageable);



    @Query(value = """
    {
      "authorId": ?0,
      "$or": [
        { "createdAt": { "$lt": ?1 } },
        { "createdAt": ?1, "_id": { "$lt": ?2 } }
      ]
    }
    """,
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Feed> getFeedForUsers(String authorId,
                               Instant cursorTime,
                               String cursorId,
                               Pageable pageable);


    void deleteByAuthorIdAndRecipientUserId(
            String authorId,
            String recipientUserId
    );
}
