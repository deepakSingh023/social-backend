package com.example.Social.profile.service;

import com.example.Social.profile.dto.SearchResponse;
import com.example.Social.profile.entity.profile;
import com.example.Social.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileSearchService {

    private final ProfileRepository profileRepository;

    public SearchResponse searchProfiles(
            String q,
            String cursor,
            int limit
    ) {

        Pageable pageable = PageRequest.of(0, limit);

        List<profile> profiles;

        if (cursor == null || cursor.isEmpty()) {

            profiles = profileRepository
                    .findByUsernameRegexIgnoreCaseOrderByIdAsc(
                            q,
                            pageable
                    );

        } else {

            profiles = profileRepository
                    .findByUsernameRegexIgnoreCaseAndIdGreaterThanOrderByIdAsc(
                            q,
                            cursor,
                            pageable
                    );
        }

        String nextCursor = null;

        if (!profiles.isEmpty()) {
            nextCursor = profiles.get(profiles.size() - 1).getId();
        }

        boolean hasMore = profiles.size() == limit;

        return new SearchResponse(
                profiles,
                nextCursor,
                hasMore
        );
    }
}