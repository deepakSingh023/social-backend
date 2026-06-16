package com.example.social_interaction.service;

import com.example.social_interaction.dto.*;
import com.example.social_interaction.entity.FollowRequest;
import com.example.social_interaction.entity.FriendRequest;
import com.example.social_interaction.entity.Friends;
import com.example.social_interaction.enums.CounterType;
import com.example.social_interaction.enums.FriendRequestStatus;
import com.example.social_interaction.repository.FriendRepository;
import com.example.social_interaction.repository.FriendRequestRepository;
import com.example.social_interaction.tasks.ChatClient;
import com.example.social_interaction.tasks.CounterClient;
import com.example.social_interaction.tasks.PostClient;
import com.example.social_interaction.tasks.ProfileClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.patterns.IToken;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;


@RequiredArgsConstructor
@Service
public class FriendServiceImpl implements FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final CounterClient counterClient;
    private final InteractonService interactonService;

    private final ConversationService conversationService;

    private final ProfileClient profileClient;

    private final DenormalizeAndFeedService denormalizeAndFeedService;



    @Value("${service.secret}")
    private String secret;

    // ---------------- ADD FRIEND ----------------

    @Override
    public void addFriend(String senderId, String receiverId) {


        Map<String, ProfileDto> profiles =
                profileClient.getProfiles(List.of(senderId, receiverId),secret);

        if (!profiles.containsKey(senderId) ||
                !profiles.containsKey(receiverId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }



        boolean alreadyFriends =
                friendRepository.existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                        senderId, receiverId,
                        receiverId, senderId
                );

        if (alreadyFriends) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already friends");
        }

        Optional<FriendRequest> existingRequest =
                friendRequestRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
                        senderId, receiverId,
                        senderId,receiverId
                );



        // Auto-accept if opposite request exists
        if (existingRequest.isPresent()) {
            FriendRequest req = existingRequest.get();

            Friends friend = Friends.builder()
                    .senderId(req.getSenderId())
                    .senderAvatar(req.getSenderAvatar())
                    .senderName(req.getSenderName())
                    .receiverId(req.getReceiverId())
                    .receiverAvatar(req.getReceiverAvatar())
                    .receiverName(req.getReceiverName())
                    .acceptedAt(Instant.now())
                    .build();

            friendRepository.save(friend);
            friendRequestRepository.delete(req);

            denormalizeAndFeedService.worker(new UpdateCounter(
                    senderId,
                    CounterType.FRIENDS,
                    1
            ),new UpdateCounter(
                    receiverId,
                    CounterType.FRIENDS,
                    1
            ),new InteractionDto(
                    senderId,
                    receiverId
            ),new InteractionDto(
                    receiverId,
                    senderId
            ));
            interactonService.createInteraction(senderId,receiverId);
            interactonService.createInteraction(receiverId,senderId);

            conversationService.createConversation(senderId,receiverId);


            return;
        }


        FriendRequest createRequest = FriendRequest.builder()
                .senderId(senderId)
                .senderAvatar(profiles.get(senderId).avatar())
                .senderName(profiles.get(senderId).username())
                .receiverId(receiverId)
                .receiverAvatar(profiles.get(receiverId).avatar())
                .receiverName(profiles.get(receiverId).username())
                .receivedAt(Instant.now())
                .build();

        friendRequestRepository.save(createRequest);
    }

    // ---------------- REMOVE FRIEND ----------------

    @Override
    public void removeFriend(String senderId, String receiverId) {

        Friends friend = friendRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                        senderId, receiverId,
                        receiverId, senderId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Not friends"));

        UpdateCounter data = new UpdateCounter(
                senderId,
                CounterType.FRIENDS,
                -1
        );

        counterClient.denormalize(data,secret);

        UpdateCounter data2 = new UpdateCounter(
                receiverId,
                CounterType.FRIENDS,
                -1
        );

        counterClient.denormalize(data2,secret);

        friendRepository.delete(friend);

        conversationService.deleteConversation(senderId,receiverId);

        interactonService.deleteInteraction(senderId,receiverId);
        interactonService.deleteInteraction(receiverId,senderId);
    }

    // ---------------- GET FRIENDS (PAGINATED) ----------------

    @Override
    public FriendResponse getFriends(
            String userId,
            String cursor
    ) {

        List<Friends> friends;

        Pageable pageable = PageRequest.of(
                0,
                11
        );

        if (cursor == null || cursor.isBlank()) {

            friends =
                    friendRepository
                            .findTop10BySenderIdOrReceiverIdOrderByAcceptedAtDescIdDesc(
                                    userId,
                                    userId
                            );

        } else {

            String[] parts = cursor.split("_", 2);

            if (parts.length != 2) {
                throw new RuntimeException("Invalid cursor");
            }

            Instant cursorDate =
                    Instant.parse(parts[0]);

            String cursorId = parts[1];

            friends =
                    friendRepository.findNextFriendsPage(
                            userId,
                            userId,
                            cursorDate,
                            cursorId,
                            pageable
                    );
        }

        boolean hasMore = friends.size()>10;

        if(hasMore){
            friends= friends.subList(0,10);
        }

        String nextCursor = null;

        if (!friends.isEmpty()) {

            Friends last =
                    friends.get(friends.size() - 1);

            nextCursor =
                    last.getAcceptedAt().toString()
                            + "_"
                            + last.getId();
        }

        List<Response> res = friends.stream()
                .map(friend->{
                    boolean isSender = userId.equals(friend.getSenderId());

                    return new Response(
                            isSender
                                    ? friend.getReceiverId()
                                    : friend.getSenderId(),

                            isSender
                                    ? friend.getReceiverName()
                                    : friend.getSenderName(),

                            isSender
                                    ? friend.getReceiverAvatar()
                                    : friend.getSenderAvatar()

                    );

                }).toList();

        return new FriendResponse(
                res,
                nextCursor,
                hasMore
        );
    }

    // ---------------- ACCEPT REQUEST ----------------

    @Override
    public void acceptRequest(String requestId, String currentUserId) {

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Friend request not found"));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        Friends friend = Friends.builder()
                .senderId(request.getSenderId())
                .senderAvatar(request.getSenderAvatar())
                .senderName(request.getSenderName())
                .receiverId(request.getReceiverId())
                .receiverAvatar(request.getReceiverAvatar())
                .receiverName(request.getReceiverName())
                .acceptedAt(Instant.now())
                .build();

        friendRepository.save(friend);
        friendRequestRepository.delete(request);



        denormalizeAndFeedService.worker(new UpdateCounter(
                currentUserId,
                CounterType.FRIENDS,
                1
        ),new UpdateCounter(
                friend.getSenderId(),
                CounterType.FRIENDS,
                1
        ),new InteractionDto(
                currentUserId,
                friend.getSenderId()
        ),new InteractionDto(
                friend.getSenderId(),
                currentUserId
        ));

        conversationService.createConversation(friend.getSenderId(),friend.getReceiverId());

        interactonService.createInteraction(currentUserId, friend.getSenderId());
        interactonService.createInteraction(friend.getSenderId(), currentUserId);

    }

    // ---------------- REJECT REQUEST ----------------

    @Override
    public void rejectRequest(String requestId, String currentUserId) {

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Friend request not found"));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        friendRequestRepository.delete(request);
    }

    // ---------------- GET REQUESTS (PAGINATED) ----------------

    @Override
    public RequestResponse getRequests(String userId, String cursor) {
        List<FriendRequest> req;

        Pageable pageable = PageRequest.of(
                0,
                11,
                Sort.by(
                        Sort.Order.desc("receivedAt"),
                        Sort.Order.desc("_id")
                )
        );

        if (cursor == null || cursor.isBlank()){

             req = friendRequestRepository.findTop10ByReceiverIdOrderByReceivedAtDescIdDesc(userId);

         }else{
             String[] parts = cursor.split("_", 2);

             if (parts.length != 2) {
                 throw new RuntimeException("Invalid cursor");
             }

             Instant cursorDate = Instant.parse(parts[0]);
             String cursorId = parts[1];

             req = friendRequestRepository.findNextPage(
                     userId,
                     cursorDate,
                     cursorId,
                     pageable
             );
         }

        String nextCursor = null;

        if (!req.isEmpty()) {

            FriendRequest last = req.get(req.size() - 1);

            nextCursor =
                    last.getReceivedAt().toString()
                            + "_"
                            + last.getId();
        }

        return new RequestResponse(req,nextCursor);
    }


    @Override
    public SearchRequest searchFriends(
            String userId,
            String cursor,
            String query
    ) {

        PageRequest pageable = PageRequest.of(0, 11);

        List<Friends> friends;

        // FIRST PAGE
        if (cursor == null || cursor.isBlank()) {

            friends = friendRepository.findFriendsFirstPage(
                    userId,
                    query == null ? "" : query,
                    pageable
            );

        } else {

            // NEXT PAGE

            String[] parts = cursor.split("_", 2);

            Instant cursorDate = Instant.parse(parts[0]);

            String cursorId = parts[1];

            friends = friendRepository.findFriendsNextPage(
                    userId,
                    query == null ? "" : query,
                    cursorDate,
                    cursorId,
                    pageable
            );
        }

        // GENERATE NEXT CURSOR

        boolean hasMore = friends.size()>10;

        if(hasMore){
            friends = friends.subList(0,10);
        }

        String nextCursor = null;

        if (!friends.isEmpty()) {

            Friends lastFriend = friends.get(friends.size() - 1);

            nextCursor =
                    lastFriend.getAcceptedAt().toString()
                            + "_"
                            + lastFriend.getId();
        }

        List<Response> res = friends.stream()
                .map(friend->{
                    boolean isSender = userId.equals(friend.getSenderId());

                    return new Response(
                            isSender
                                    ? friend.getReceiverId()
                                    : friend.getSenderId(),

                            isSender
                                    ? friend.getReceiverName()
                                    : friend.getSenderName(),

                            isSender
                                    ? friend.getReceiverAvatar()
                                    : friend.getSenderAvatar()

                    );

                        }).toList();

        return new SearchRequest(
                res,
                nextCursor,
                hasMore
        );
    }

}
