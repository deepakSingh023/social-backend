package com.example.Social.profile.controller;

import com.example.Social.profile.dto.SearchResponse;
import com.example.Social.profile.service.ProfileSearchService;
import com.example.Social.profile.service.profileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profile/")
public class SearchController {

    private final ProfileSearchService profileSearchService;

    @GetMapping("/search")
    public SearchResponse searchProfiles(
            @RequestParam String q,
            @RequestParam(required = false)
            String cursor,
            @RequestParam(defaultValue = "10")
            int limit
    ) {

        return profileSearchService.searchProfiles(
                q,
                cursor,
                limit
        );
    }
}
