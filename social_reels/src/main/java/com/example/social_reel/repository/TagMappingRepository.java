package com.example.social_reel.repository;

import com.example.social_reel.entity.TagMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TagMappingRepository
        extends MongoRepository<TagMapping, String> {

    Optional<TagMapping> findByRawTag(String rawTag);
}
