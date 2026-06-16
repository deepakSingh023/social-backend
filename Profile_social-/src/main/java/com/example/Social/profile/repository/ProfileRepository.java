package com.example.Social.profile.repository;

import com.example.Social.profile.entity.profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends MongoRepository<profile, String> {

    Optional<profile> findByUserId(String userId);

    List<profile> findByUserIdIn(List<String> userIds);

    List<profile> findByUsernameRegexIgnoreCaseAndIdGreaterThanOrderByIdAsc(
            String username,
            String id,
            Pageable pageable
    );

    List<profile> findByUsernameRegexIgnoreCaseOrderByIdAsc(
            String username,
            Pageable pageable
    );

}
