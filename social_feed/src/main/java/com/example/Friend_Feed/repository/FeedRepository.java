package com.example.Friend_Feed.repository;

import com.example.Friend_Feed.entity.Feed;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface FeedRepository extends MongoRepository<Feed,String> {

   List<Feed> findTop11ByFeedOwnerIdOrderByCreatedAtDescIdDesc(String ownerId, Pageable pageable);


    @Query(value = """
    {
      "feedOwnerId": ?0,
      "$or": [
        { "createdAt": { "$lt": ?1 } },
        { "createdAt": ?1, "_id": { "$lt": ?2 } }
      ]
    }
    """,
            sort = "{ 'createdAt': -1, '_id': -1 }")
   List<Feed> getFeed(String ownerId,
                      Instant cursor,
                      ObjectId cursorId,
                      Pageable pageable);
}
