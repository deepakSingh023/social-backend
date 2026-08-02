package com.example.social_interaction.controller;

import com.example.social_interaction.dto.FriendResponse;
import com.example.social_interaction.dto.RequestResponse;
import com.example.social_interaction.dto.friendRequest;
import com.example.social_interaction.entity.FriendRequest;
import com.example.social_interaction.entity.Friends;
import com.example.social_interaction.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * Send a friend request (or auto-accept if reverse request exists)
     */
    @PostMapping("/{receiverId}")
    public ResponseEntity<Void> addFriend(
            @PathVariable String receiverId,
            @RequestHeader("X-User-Id") String senderId
    ) {
        friendService.addFriend(senderId, receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Remove a friend
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable String friendId,
            @RequestHeader("X-User-Id") String userId
    ) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<FriendResponse> getMyFriends(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false)
            String cursor
    ) {

        return ResponseEntity.ok(
                friendService.getFriends(userId, cursor)
        );
    }

    /**
     * Accept a friend request
     */
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable String requestId,
            @RequestHeader("X-User-Id") String userId
    ) {
        friendService.acceptRequest(requestId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject a friend request
     */
    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable String requestId,
            @RequestHeader("X-User-Id") String userId
    ) {
        friendService.rejectRequest(requestId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get my incoming friend requests (paginated)
     */
    @GetMapping("/requests")
    public ResponseEntity<RequestResponse> getMyFriendRequests(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(
                friendService.getRequests(userId, cursor)
        );
    }

}
