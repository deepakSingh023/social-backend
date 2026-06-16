package com.example.social_chat.repository;

import com.example.social_chat.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation,String> {


    boolean existsByUserId1AndUserId2(
            String userId1,
            String userId2
    );

    Optional<Conversation> findByUserId1AndUserId2(String userId1 , String userId2);
}
