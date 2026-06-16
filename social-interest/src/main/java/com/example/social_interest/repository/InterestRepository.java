package com.example.social_interest.repository;

import com.example.social_interest.entity.UserInterest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface InterestRepository extends MongoRepository<UserInterest, String> {

    Optional<UserInterest> findByUserId(String userId);
}
