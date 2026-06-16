package com.example.social_interaction.controller;


import com.example.social_interaction.dto.FollowResult;
import com.example.social_interaction.dto.RequestResponse;
import com.example.social_interaction.dto.SearchRequest;
import com.example.social_interaction.enums.FollowerType;
import com.example.social_interaction.repository.FriendRepository;
import com.example.social_interaction.service.FollowService;
import com.example.social_interaction.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final FriendService friendService;

    private final FollowService followService;


    @GetMapping("/friend")
    public ResponseEntity<SearchRequest> search(
            Authentication authentication,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String query
    ){

        String userId = authentication.getName();

        return ResponseEntity.ok(

                friendService.searchFriends(userId,cursor,query)
        );


    }

    @GetMapping("/followers")
    public ResponseEntity<FollowResult> getConnections(
            Authentication authentication,
            @RequestParam FollowerType type,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cursor
    ) {

        String userId = authentication.getName();

        return ResponseEntity.ok(
                followService.searchConnections(
                        userId,
                        type,
                        query,
                        cursor
                )
        );
    }
}
