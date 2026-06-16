package com.example.social_chat.services;


import com.example.social_chat.dto.ConversationDto;
import com.example.social_chat.entity.Conversation;
import com.example.social_chat.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;


@RequiredArgsConstructor
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    private final MongoTemplate mongoTemplate;


    public void createCOnvo(ConversationDto data){

        String userId1 = data.user1Id().compareTo(data.user2Id())<0? data.user1Id() : data.user2Id();

        String userId2 = data.user1Id().compareTo(data.user2Id())<0? data.user2Id() : data.user1Id();

        if (conversationRepository.existsByUserId1AndUserId2(userId1, userId2)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conversation already exists"
            );
        }


        Conversation convo = Conversation.builder()
                .userId1(userId1)
                .userId2(userId2)
                .createdAt(Instant.now())
                .build();

        conversationRepository.save(convo);

    }

    public void deleteConvo(ConversationDto data){

        String userId1 = data.user1Id().compareTo(data.user2Id())<0? data.user1Id() : data.user2Id();

        String userId2 = data.user1Id().compareTo(data.user2Id())<0? data.user2Id() : data.user1Id();


        Query query = Query.query(Criteria.where("userId1").is(userId1).and("userId2").is(userId2));

        mongoTemplate.remove(query, Conversation.class);


    }

    public String getConvId(String senderId, String receiverId){

        String userId1 = senderId.compareTo(receiverId)<0? senderId: receiverId;
        String userId2 = senderId.compareTo(receiverId)<0? receiverId: senderId;


        Conversation conversation = conversationRepository.findByUserId1AndUserId2(userId1,userId2)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"conversation not found"));


        return conversation.getId();

    }


}
