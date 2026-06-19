package com.example.social_interest.service;

import com.example.social_interest.dto.InterestDto;
import com.example.social_interest.dto.InterestRequest;
import com.example.social_interest.entity.InterestScore;
import com.example.social_interest.entity.UserInterest;
import com.example.social_interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;

    private static final Logger log = LoggerFactory.getLogger(InterestServiceImpl.class);

    @Override
    public void updateInterest(String userId, InterestRequest request) {
        Set<String> semanticTags = request.getSemanticTags();

        if (semanticTags == null || semanticTags.isEmpty()) {
            log.warn("No tags found. Exiting interest update.");
            return;
        }

        UserInterest userInterest = interestRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    UserInterest ui = new UserInterest();
                    ui.setUserId(userId);
                    ui.setLastUpdated(Instant.now());
                    return ui;
                });


        Instant now = Instant.now();

        for (String tag : semanticTags) {
            InterestScore score = userInterest.getInterests()
                    .getOrDefault(tag, new InterestScore(0.1, now));

            long hoursPassed = Duration
                    .between(score.getLastUpdated(), now)
                    .toHours();

            double decayFactor = Math.exp(-0.03 * hoursPassed);
            double decayedScore = score.getScore() * decayFactor;

            double boost = boostForEvent(request.getType());

            double boostedScore = decayedScore + boost;

            boostedScore = Math.max(0.0, Math.min(1.0, boostedScore));

            score.setScore(boostedScore);
            score.setLastUpdated(now);

            userInterest.getInterests().put(tag, score);
        }

        userInterest.setLastUpdated(now);

        interestRepository.save(userInterest);
    }


    @Override
    public InterestDto getInterest(String userId){

        UserInterest interest = interestRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserInterest ui = new UserInterest();
                    ui.setUserId(userId);
                    ui.setInterests(new HashMap<>());
                    ui.setLastUpdated(Instant.now());
                    return ui;
                });

        return new InterestDto(
                interest.getId(),
                interest.getUserId(),
                interest.getInterests(),
                interest.getLastUpdated()
        );
    }

    private double boostForEvent(String event) {
        return switch (event) {
            case "WATCH_50" -> 0.06;
            case "WATCH_90" -> 0.12;
            case "LIKE"     -> 0.20;
            case "SHARE"    -> 0.30;
            default         -> 0.03;
        };
    }
}
