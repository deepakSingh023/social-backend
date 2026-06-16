package com.example.social_interaction.repository;

import com.example.social_interaction.entity.Follower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface  RelationRepository extends MongoRepository<Follower,String> {

    boolean existsByUserIdAndFollowedId(String userId ,String FollowedId);

    Optional<Follower> findByUserIdAndFollowedId(String userId , String followedId);
    //the user is the person being followed and the follower is the current user

    Page<Follower> findByUserId(String userId, Pageable pageable);
    Page<Follower> findByFollowedId(String followedId, Pageable pageable);

    @Query(value = """
{
  "$and": [
    {
      "followedId": ?0
    },
    {
      "userName": {
        "$regex": ?1,
        "$options": "i"
      }
    }
  ]
}
""",
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Follower> findFollowersFirstPage(
            String userId,
            String query,
            Pageable pageable
    );

    @Query(value = """
{
  "$and": [
    {
      "followedId": ?0
    },
    {
      "userName": {
        "$regex": ?1,
        "$options": "i"
      }
    },
    {
      "$or": [
        {
          "createdAt": { "$lt": ?2 }
        },
        {
          "createdAt": ?2,
          "_id": { "$lt": ?3 }
        }
      ]
    }
  ]
}
""",
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Follower> findFollowersNextPage(
            String userId,
            String query,
            Instant cursorDate,
            String cursorId,
            Pageable pageable
    );


    @Query(value = """
{
  "$and": [
    {
      "userId": ?0
    },
    {
      "followedName": {
        "$regex": ?1,
        "$options": "i"
      }
    }
  ]
}
""",
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Follower> findFollowingFirstPage(
            String userId,
            String query,
            Pageable pageable
    );

    @Query(value = """
{
  "$and": [
    {
      "userId": ?0
    },
    {
      "followedName": {
        "$regex": ?1,
        "$options": "i"
      }
    },
    {
      "$or": [
        {
          "createdAt": { "$lt": ?2 }
        },
        {
          "createdAt": ?2,
          "_id": { "$lt": ?3 }
        }
      ]
    }
  ]
}
""",
            sort = "{ 'createdAt': -1, '_id': -1 }")
    List<Follower> findFollowingNextPage(
            String userId,
            String query,
            Instant cursorDate,
            String cursorId,
            Pageable pageable
    );



}
