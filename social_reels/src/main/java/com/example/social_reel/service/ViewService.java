package com.example.social_reel.service;


import com.example.social_reel.dto.*;
import com.example.social_reel.entity.Reel;
import com.example.social_reel.enums.InterestType;
import com.example.social_reel.exceptions.ReelNotFound;
import com.example.social_reel.repository.ReelRepository;
import com.example.social_reel.util.LikeClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewIncrement viewIncrement;
    private final ReelRepository reelRepository;

    private final LikeClient likeClient;

    @Value("${service.secret}")
    private String token;

    private final static Logger log = LoggerFactory.getLogger(ViewService.class);

    public Set<String> viewUpdate(ViewDto data){


        Reel reel;
        if(data.type()==InterestType.LIKE){
            reel = reelRepository.findById(data.reelId())
                    .orElseThrow( ()-> new ReelNotFound("reel not found"));

        }else{
             reel = viewIncrement.incrementView(data.reelId());
        }



        if (reel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reel not found");
        }




        double popularity = calculatePopularity(reel);

        reel.setPopularityScore(popularity);

        reelRepository.save(reel);

        return reel.getSemanticTags();
    }



    public FeedResponse fetchFeed(FetchReelDto req){

        UserInterest interest = req.data();
        int limit = req.req().limit();

        Instant cursorTime = req.req().cursor() != null
                ? Instant.parse(req.req().cursor())
                : Instant.now();

        Set<String> topTags = interest != null
                ? getTopTags(interest, 5)
                : Set.of();

        List<Reel> feed = new ArrayList<>();

        if (!topTags.isEmpty()) {
            feed.addAll(
                    reelRepository.findBySemanticTagsInAndCreatedAtLessThanOrderByCreatedAtDesc(
                            topTags,
                            cursorTime,
                            PageRequest.of(0, limit / 2)
                    )
            );
        }

        feed.addAll(
                reelRepository.findByCreatedAtLessThanOrderByPopularityScoreDesc(
                        cursorTime,
                        PageRequest.of(0, limit / 4)
                )
        );

        feed.addAll(
                reelRepository.findByCreatedAtLessThanOrderByCreatedAtDesc(
                        cursorTime,
                        PageRequest.of(0, limit / 4)
                )
        );

        Collections.shuffle(feed);



        List<Reel> uniqueFeed =
                feed.stream()
                        .distinct()
                        .limit(limit)
                        .toList();


        List<String> reelIds = uniqueFeed.stream()
                .map(Reel::getId)
                .toList();




        Map<String, Boolean> isLiked = new HashMap<>();

        try {
            isLiked = likeClient.getLikedStatus(
                    token,
                    interest.userId(),
                    reelIds
            );
        } catch (Exception e) {

            log.warn("Like service unavailable", e);

            for (String id : reelIds) {
                isLiked.put(id, false);
            }
        }

        final Map<String, Boolean> finalIsLiked = isLiked;





        List<ReelResponse> response =
                uniqueFeed.stream()
                        .map(r -> new ReelResponse(
                                r.getId(),
                                r.getUsername(),
                                r.getAvatar(),
                                r.getVideoUrl(),
                                r.getRawTags(),
                                r.getViewCount(),
                                r.getCreatedAt(),
                                r.getUserId(),
                                finalIsLiked.getOrDefault(r.getId(),false)
                        ))
                        .toList();

        // create next cursor
        String nextCursor = null;

        if(!uniqueFeed.isEmpty()){
            nextCursor =
                    uniqueFeed
                            .get(uniqueFeed.size()-1)
                            .getCreatedAt()
                            .toString();
        }

        return new FeedResponse(response,nextCursor);

    }

    private Set<String> getTopTags(UserInterest interest, int n) {
        return interest.interests()
                .entrySet()
                .stream()
                .sorted((a, b) ->
                        Double.compare(
                                b.getValue().score(),
                                a.getValue().score()
                        )
                )
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private double calculatePopularity(Reel reel) {

        long hours =
                Duration.between(reel.getCreatedAt(), Instant.now()).toHours() + 1;

        double engagementScore =
                reel.getViewCount()
                        + (reel.getLikes() * 5);

        return engagementScore / Math.pow(hours, 1.5);
    }
}