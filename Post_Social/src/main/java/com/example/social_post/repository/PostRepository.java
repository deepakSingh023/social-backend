package com.example.social_post.repository;

import com.example.social_post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    // Custom query method: find all posts for a given userId
    List<Post> findByUserId(String userId);

    List<Post> findAllById(Iterable<String> ids);

    List<Post> findByUserIdOrderByCreatedAtDescIdDesc(
            String authorId,
            Pageable pageable
    );

    List<Post> findByUserIdOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );


    List<Post> findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            String userId,
            Instant cursorCreatedAt,
            Pageable pageable
    );




    @Query(value = """
    {
      "userId": ?0,
      "$or": [
        { "createdAt": { "$lt": ?1 } },
        { "createdAt": ?1, "_id": { "$lt": ?2 } }
      ]
    }
    """,
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Post> findNextPostsByAuthorId(
            String authorId,
            Instant cursorCreatedAt,
            String cursorId,
            Pageable pageable
    );
}
