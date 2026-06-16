package com.example.social_view.service;


import com.example.social_view.dto.InterestDto;
import com.example.social_view.dto.ViewDto;
import com.example.social_view.enums.InterestType;
import com.example.social_view.util.InterestClient;
import com.example.social_view.util.ReelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.social_view.entity.Reel;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {


//    private final IncrementService incrementService;

    private final InterestClient interestClient;

    private final ReelClient reelClient;

    @Value("${service.secret}")
    private String secret;




    @Override
    public void registerView(ViewDto data, String userId) {


//        incrementService.incrementView(data.reelId());
//        reel.setPopularityScore(calculatePopularity(reel));
//
//        reelRepository.save(reel);

        Set<String> tags = reelClient.getReel(data,secret);

        InterestDto data2 = new InterestDto(
                userId,
                data.reelId(),
                data.type(),
                tags
        );

        interestClient.sendInterest(data2,secret);
    }

    @Override
    public void registerInterestLikes(String reelId, String userId){

        Set<String> tags = reelClient.getReel(new ViewDto(reelId, InterestType.LIKE),secret);

        InterestDto data2 = new InterestDto(
                userId,
                reelId,
                InterestType.LIKE,
                tags
        );

        interestClient.sendInterest(data2,secret);

    }

//    private double calculatePopularity(Reel reel) {
//        long hours =
//                Duration.between(reel.getCreatedAt(), Instant.now()).toHours() + 1;
//
//        return (reel.getViewCount()) / Math.pow(hours, 1.5);
//    }
}
