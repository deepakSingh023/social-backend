package com.example.social_fetchReels.service;

import com.example.social_fetchReels.dto.FeedRequest;
import com.example.social_fetchReels.dto.FeedResponse;
import com.example.social_fetchReels.dto.FetchReelDto;
import com.example.social_fetchReels.dto.ReelResponse;
import com.example.social_fetchReels.entity.Reel;
import com.example.social_fetchReels.entity.UserInterest;
import com.example.social_fetchReels.util.InterestClient;
import com.example.social_fetchReels.util.ReelClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReelServiceImpl implements ReelService {

    private static final Logger log = LoggerFactory.getLogger(ReelServiceImpl.class);

    private final InterestClient interestClient;

    private final ReelClient reelClient;

    @Value("${secret.service}")
    private String token;


    @Override
    public FeedResponse getFeed(String userId, String cursor, int limit) {


        UserInterest interest;

        try {
            interest = interestClient.getInterest(userId, token);

            log.info("Interest fetched successfully");
            log.info("Interest = {}", interest);

        } catch (FeignException ex) {

            log.error("Feign Error");
            log.error("Status = {}", ex.status());
            log.error("Body = {}", ex.contentUTF8());

            throw ex;
        }

        FeedRequest req = new FeedRequest();
        req.setCursor(cursor);
        req.setLimit(limit);

        FetchReelDto data = new FetchReelDto(
                interest,
                req
        );


        FeedResponse res = reelClient.getFeed(data,token);

        return res;
    }

}
