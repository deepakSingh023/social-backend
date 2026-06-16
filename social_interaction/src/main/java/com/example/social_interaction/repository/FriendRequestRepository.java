package com.example.social_interaction.repository;

import com.example.social_interaction.entity.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository
        extends MongoRepository<FriendRequest, String> {

    List<FriendRequest>
    findTop10ByReceiverIdOrderByReceivedAtDescIdDesc(
            String receiverId
    );



    @Query(value = """
    {
      "receiverId": ?0,
      "$or": [
        {
          "receivedAt": { "$lt": ?1 }
        },
        {
          "receivedAt": ?1,
          "_id": { "$lt": ?2 }
        }
      ]
    }
    """,
            sort = "{ 'receivedAt': -1, '_id': -1 }")
    List<FriendRequest> findNextPage(
            String receiverId,
            Instant cursorDate,
            String cursorId,
            Pageable pageable
    );



    Page<FriendRequest> findBySenderId(
            String senderId,
            Pageable pageable
    );



    Optional<FriendRequest>
    findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
            String sender1Id,
            String receiver1Id,
            String sender2Id,
            String receiver2Id
    );
}