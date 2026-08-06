package com.example.Social.profile.repository;

import com.example.Social.profile.entity.Outbox;
import com.example.Social.profile.enums.EventStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxRepository extends MongoRepository<Outbox,String> {

    List<Outbox> findTop100ByStatusOrderByCreatedAt(EventStatus status);
}
