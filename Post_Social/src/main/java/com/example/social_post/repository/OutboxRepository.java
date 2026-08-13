package com.example.social_post.repository;


import com.example.social_post.entity.Outbox;
import com.example.social_post.enums.EventStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxRepository extends MongoRepository<Outbox,String> {

    List<Outbox> findTop100ByStatusOrderByCreatedAt(EventStatus status);
}
