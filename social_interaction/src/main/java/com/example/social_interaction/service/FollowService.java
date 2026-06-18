package com.example.social_interaction.service;
import com.example.social_interaction.dto.*;
import com.example.social_interaction.entity.FollowRequest;
import com.example.social_interaction.entity.Follower;
import com.example.social_interaction.enums.CounterType;
import com.example.social_interaction.enums.FollowerType;
import com.example.social_interaction.repository.FollowRequestRepository;
import com.example.social_interaction.repository.RelationRepository;
import com.example.social_interaction.tasks.CounterClient;
import com.example.social_interaction.tasks.PostClient;
import com.example.social_interaction.tasks.ProfileClient;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static java.time.LocalTime.now;

@Service
@RequiredArgsConstructor
public class FollowService implements RelationService{


  private final RelationRepository relationRepository;

  private final ProfileClient profileClient;

  private final FollowRequestRepository followRequestRepository;

  private final InteractonService interactonService;

  private final CounterClient counterClient;


  private final DenormalizeAndFeedService denormalizeAndFeedService;

  private final static Logger log = LoggerFactory.getLogger(FollowService.class);

  @Value("${service.secret}")
  private String secret;

    @Override
    public void followRequest(String userId, followRequest request) {

        Map<String, ProfileDto> profiles =
                profileClient.getProfiles(List.of(userId, request.getFollowedId()),secret);

        if (!profiles.containsKey(userId) ||
                !profiles.containsKey(request.getFollowedId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        if (request.getFollowedId().equals(userId)) {
            throw new IllegalArgumentException("You cannot send a follow request to yourself");
        }

        // Already following
        if (relationRepository.existsByUserIdAndFollowedId(userId, request.getFollowedId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already following");
        }




        if (Boolean.TRUE.equals(request.getPrvAcc())) {

            if (followRequestRepository
                    .existsByUserIdAndFollowedId(userId, request.getFollowedId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Follow request already sent");
            }

            FollowRequest createRequest = FollowRequest.builder()
                    .userId(userId)
                    .userAvatar(profiles.get(userId).avatar())
                    .userName(profiles.get(userId).username())
                    .followedId(request.getFollowedId())
                    .followedAvatar(profiles.get(request.getFollowedId()).avatar())
                    .followedName(profiles.get(request.getFollowedId()).username())
                    .createdAt(Instant.now())
                    .build();

            followRequestRepository.save(createRequest);
            return;
        }


        Follower follower = Follower.builder()
                .userId(userId)
                .userAvatar(profiles.get(userId).avatar())
                .userName(profiles.get(userId).username())
                .followedId(request.getFollowedId())
                .followedAvatar(profiles.get(request.getFollowedId()).avatar())
                .followedName(profiles.get(request.getFollowedId()).username())
                .createdAt(Instant.now())
                .build();

        relationRepository.save(follower);

        interactonService.createInteraction(follower.getFollowedId(),follower.getUserId());
        

        denormalizeAndFeedService.followerWorker(new UpdateCounter(
                userId,
                CounterType.FOLLOWING,
                1
        ),new UpdateCounter(
                request.getFollowedId(),
                CounterType.FOLLOWER,
                1
        ),new InteractionDto(
                follower.getFollowedId(),follower.getUserId()
        ));

    }

    @Override
    public void acceptFollowRequest(String requestId) {

        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Follow request not found"));

        String userId = request.getUserId();        // sender
        String followedId = request.getFollowedId(); // receiver (me)

        // Prevent duplicate followers
        if (relationRepository.existsByUserIdAndFollowedId(userId, followedId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already following");
        }

        // Create follower relationship
        Follower follower = Follower.builder()
                .userId(userId)
                .userAvatar(request.getUserAvatar())
                .userName(request.getUserName())
                .followedId(followedId)
                .followedAvatar(request.getFollowedAvatar())
                .followedName(request.getFollowedName())
                .createdAt(Instant.now())
                .build();

        relationRepository.save(follower);

        followRequestRepository.deleteById(requestId);


        denormalizeAndFeedService.followerWorker(new UpdateCounter(
                userId,
                CounterType.FOLLOWING,
                1
        ),new UpdateCounter(
                request.getFollowedId(),
                CounterType.FOLLOWER,
                1
        ),new InteractionDto(
                follower.getFollowedId(),follower.getUserId()
        ));

        interactonService.createInteraction(followedId,userId);



    }


    @Override
    public void rejectFollowRequest(String requestId) {

        if (!followRequestRepository.existsById(requestId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Follow request not found");
        }

        followRequestRepository.deleteById(requestId);
    }

    @Override
    public void stopFollowing(String userId, String followedId) {
        Follower follower = relationRepository
                .findByUserIdAndFollowedId(userId, followedId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No follow found"));

        relationRepository.delete(follower);

        UpdateCounter data = new UpdateCounter(
                userId,
                CounterType.FOLLOWING,
                -1
        );


        try {
            counterClient.denormalize(data, secret);
        } catch (Exception e) {
            log.error("counter update failed for user1 = {}", data.userId(), e);
        }


        UpdateCounter data2 = new UpdateCounter(
                followedId,
                CounterType.FOLLOWER,
                -1
        );

        try {
            counterClient.denormalize(data2, secret);
        } catch (Exception e) {
            log.error("counter update failed for user2 = {}", data2.userId(), e);
        }


        interactonService.deleteInteraction(followedId,userId);

    }

    @Override
    public void removeFollower(String followedById, String userId) {
        Follower follower = relationRepository
                .findByUserIdAndFollowedId(followedById, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No follow found"));

        relationRepository.delete(follower);

        UpdateCounter data = new UpdateCounter(
                followedById,
                CounterType.FOLLOWING,
                -1
        );

        try {
            counterClient.denormalize(data, secret);
        } catch (Exception e) {
            log.error("counter update failed for user1 = {}", data.userId(), e);
        }

        UpdateCounter data2 = new UpdateCounter(
                userId,
                CounterType.FOLLOWER,
                -1
        );

        try {
            counterClient.denormalize(data2, secret);
        } catch (Exception e) {
            log.error("counter update failed for user2 = {}", data2.userId(), e);
        }



        interactonService.deleteInteraction(userId,followedById);
    }

    @Override
    public FollowResult searchConnections(
            String userId,
            FollowerType type,
            String query,
            String cursor
    ) {

        PageRequest pageable = PageRequest.of(0, 11);

        List<Follower> followers;

        boolean firstPage =
                cursor == null || cursor.isBlank();

        switch (type) {

            case FOLLOWERS -> {

                if (firstPage) {

                    followers = relationRepository
                            .findFollowersFirstPage(
                                    userId,
                                    query == null ? "" : query,
                                    pageable
                            );

                } else {

                    String[] parts = cursor.split("_", 2);

                    Instant cursorDate =
                            Instant.parse(parts[0]);

                    String cursorId = parts[1];

                    followers = relationRepository
                            .findFollowersNextPage(
                                    userId,
                                    query == null ? "" : query,
                                    cursorDate,
                                    cursorId,
                                    pageable
                            );
                }


            }

            case FOLLOWING -> {

                if (firstPage) {

                    followers = relationRepository
                            .findFollowingFirstPage(
                                    userId,
                                    query == null ? "" : query,
                                    pageable
                            );

                } else {

                    String[] parts = cursor.split("_", 2);

                    Instant cursorDate =
                            Instant.parse(parts[0]);

                    String cursorId = parts[1];

                    followers = relationRepository
                            .findFollowingNextPage(
                                    userId,
                                    query == null ? "" : query,
                                    cursorDate,
                                    cursorId,
                                    pageable
                            );
                }
            }

            default -> throw new RuntimeException("Invalid type");
        }

        boolean hasMore = followers.size() > 10;

        if (hasMore) {

            followers = followers.subList(0, 10);
        }


        String nextCursor = null;

        if (!followers.isEmpty()) {

            Follower last =
                    followers.get(followers.size() - 1);

            nextCursor =
                    last.getCreatedAt()
                            + "_"
                            + last.getId();


        }



        return new FollowResult(
                followers,
                nextCursor,
                hasMore
        );
    }


    @Override
    public Page<FollowRequest> getFollowRequests(String userId , Pageable pageable){
    return followRequestRepository.findByFollowedId(userId , pageable);

    }

    @Override
    public Page<Follower> getFollowing(String userId, Pageable pageable) {
        return relationRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Follower> getFollowers(String userId, Pageable pageable) {
        return relationRepository.findByFollowedId(userId, pageable);
    }

}


