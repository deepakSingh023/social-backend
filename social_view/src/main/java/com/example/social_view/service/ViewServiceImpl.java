package com.example.social_view.service;


import com.example.social_view.aspect.LogAspect;
import com.example.social_view.dto.InterestDto;
import com.example.social_view.dto.ViewDto;
import com.example.social_view.enums.InterestType;
import com.example.social_view.util.InterestClient;
import com.example.social_view.util.ReelClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.social_view.entity.Reel;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

    private final ReelClient reelClient;

    private final IntereestAsyncService intereestAsyncService;


    private final static Logger log = LoggerFactory.getLogger(ViewServiceImpl.class);

    @Value("${service.secret}")
    private String secret;


    @Override
    public void registerView(ViewDto data, String userId) {

        Set<String> tags = reelClient.getReel(data, secret);

        InterestDto data2 = new InterestDto(
                userId,
                data.reelId(),
                data.type().toString(),
                tags
        );

        intereestAsyncService.updateInterest(data2);
    }

    @Override
    public void registerInterestLikes(String reelId, String userId) {

        Set<String> tags = reelClient.getReel(new ViewDto(reelId, InterestType.LIKE), secret);


        InterestDto data2 = new InterestDto(
                userId,
                reelId,
                "LIKE",
                tags
        );

        intereestAsyncService.updateInterest(data2);

    }

}
