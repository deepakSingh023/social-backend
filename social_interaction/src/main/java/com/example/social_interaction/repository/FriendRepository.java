package com.example.social_interaction.repository;

import com.example.social_interaction.entity.Friends;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends MongoRepository<Friends,String> {


    List<Friends> findBySenderIdOrReceiverId(
            String senderId,
            String receiverId,
            String cursor
    );

    List<Friends>
    findTop10BySenderIdOrReceiverIdOrderByAcceptedAtDescIdDesc(
            String senderId,
            String receiverId
    );

    @Query(value = """
{
  "$and": [
    {
      "$or": [
        { "senderId": ?0 },
        { "receiverId": ?1 }
      ]
    },
    {
      "$or": [
        {
          "acceptedAt": { "$lt": ?2 }
        },
        {
          "acceptedAt": ?2,
          "_id": { "$lt": ?3 }
        }
      ]
    }
  ]
}
""",
            sort = "{ 'acceptedAt': -1, '_id': -1 }")
    List<Friends> findNextFriendsPage(
            String senderId,
            String receiverId,
            Instant cursorDate,
            String cursorId,
            Pageable pageable
    );




    @Query(value = """
{
  "$and": [
    {
      "$or": [
        { "senderId": ?0 },
        { "receiverId": ?0 }
      ]
    },
    {
      "$or": [
        { "senderName": { "$regex": ?1, "$options": "i" } },
        { "receiverName": { "$regex": ?1, "$options": "i" } }
      ]
    }
  ]
}
""",
            sort = "{ 'acceptedAt': -1, '_id': -1 }")
    List<Friends> findFriendsFirstPage(
            String userId,
            String query,
            Pageable pageable
    );

    @Query(value = """
{
  "$and": [
    {
      "$or": [
        { "senderId": ?0 },
        { "receiverId": ?0 }
      ]
    },
    {
      "$or": [
        { "senderName": { "$regex": ?1, "$options": "i" } },
        { "receiverName": { "$regex": ?1, "$options": "i" } }
      ]
    },
    {
      "$or": [
        {
          "acceptedAt": { "$lt": ?2 }
        },
        {
          "acceptedAt": ?2,
          "_id": { "$lt": ?3 }
        }
      ]
    }
  ]
}
""",
            sort = "{ 'acceptedAt': -1, '_id': -1 }")
    List<Friends> findFriendsNextPage(
            String userId,
            String query,
            Instant cursorDate,
            String cursorId,
            Pageable pageable
    );


    Optional<Friends> findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
            String sender1, String receiver1,
            String sender2, String receiver2
    );

    boolean existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
            String sender1, String receiver1,
            String sender2, String receiver2
    );

}

