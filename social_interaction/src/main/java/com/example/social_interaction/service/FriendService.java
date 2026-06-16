package com.example.social_interaction.service;

import com.example.social_interaction.dto.FriendResponse;
import com.example.social_interaction.dto.RequestResponse;
import com.example.social_interaction.dto.SearchRequest;
import com.example.social_interaction.dto.friendRequest;
import com.example.social_interaction.entity.FriendRequest;
import com.example.social_interaction.entity.Friends;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FriendService {

    // Send friend request or auto-accept if inverse request exists
    void addFriend(String senderId, String receiverId);

    // Remove an existing friend
    void removeFriend(String senderId, String receiverId);

    // Get all friends of a user (paginated)
    FriendResponse getFriends(String userId, String cursor);

    // Accept a friend request (must be receiver)
    void acceptRequest(String requestId, String currentUserId);

    // Reject a friend request (must be receiver)
    void rejectRequest(String requestId, String currentUserId);

    // Get incoming friend requests (paginated)
    RequestResponse getRequests(String userId, String cursor);

    SearchRequest searchFriends(String userId, String cursor, String query);

}
