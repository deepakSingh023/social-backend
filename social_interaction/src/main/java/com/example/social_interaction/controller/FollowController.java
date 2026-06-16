package com.example.social_interaction.controller;

import com.example.social_interaction.dto.followRequest;
import com.example.social_interaction.entity.FollowRequest;
import com.example.social_interaction.entity.Follower;
import com.example.social_interaction.service.RelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relations")
@RequiredArgsConstructor
public class FollowController {

    private final RelationService relationService;

    /**
     * Follow a user
     * (current user is taken from JWT)
     */
    @PostMapping("/follow")
    public ResponseEntity<Void> follow(
            @RequestBody followRequest request,

            Authentication authentication
    ) {
        String userId = authentication.getPrincipal().toString();
        relationService.followRequest(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/unfollow")
    public ResponseEntity<Void> unfollow(
            @RequestParam String followedId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        relationService.stopFollowing(userId, followedId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove someone from my followers
     */
    @DeleteMapping("/followers/{followerId}")
    public ResponseEntity<Void> removeFollower(
            @PathVariable String followerId,
            Authentication authentication
    ) {
        String userId = authentication.getPrincipal().toString();
        relationService.removeFollower(followerId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Accept a follow request
     */
    @PostMapping("/follow-requests/{requestId}/accept")
    public ResponseEntity<Void> acceptFollowRequest(
            @PathVariable String requestId,
            Authentication authentication
    ) {
        String userId = authentication.getPrincipal().toString();
        relationService.acceptFollowRequest(requestId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a follow request
     */
    @DeleteMapping("/follow-requests/{requestId}")
    public ResponseEntity<Void> rejectFollowRequest(
            @PathVariable String requestId,
            Authentication authentication
    ) {
        String userId = authentication.getPrincipal().toString();
        relationService.rejectFollowRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get my followers (paginated)
     */
    @GetMapping("/me/followers")
    public ResponseEntity<Page<Follower>> getMyFollowers(
            Authentication authentication,
            Pageable pageable
    ) {
        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok(
                relationService.getFollowers(userId, pageable)
        );
    }

    /**
     * Get users I am following (paginated)
     */
    @GetMapping("/me/following")
    public ResponseEntity<Page<Follower>> getMyFollowing(
            Authentication authentication,
            Pageable pageable
    ) {
        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok(
                relationService.getFollowing(userId, pageable)
        );
    }

    /**
     * Get my pending follow requests (paginated)
     */
    @GetMapping("/me/follow-requests")
    public ResponseEntity<Page<FollowRequest>> getMyFollowRequests(
            Authentication authentication,
            Pageable pageable
    ) {
        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok(
                relationService.getFollowRequests(userId, pageable)
        );
    }
}
