package com.example.social_interaction.service;


import com.example.social_interaction.dto.RecipientPage;
import com.example.social_interaction.entity.Feed;
import com.example.social_interaction.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


//purpose of this service is to be used by the feed service to batch fetch the interaction to create feed for users who follow someone and the person they follows create a post

@RequiredArgsConstructor
@Service
public class FeedInteractionFetch {


    private final FeedRepository feedRepository;

    public RecipientPage getInteractions(String userId, int size ,String cursor){

            List<Feed> feeds = new ArrayList<>();


        if(cursor != null && !cursor.isEmpty()){
            feeds = feedRepository.findTop100ByAuthorIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(0,size));


        }else{
            String[] parts = cursor.split("\\|");
            Instant cursorCreatedAt = Instant.parse(parts[0]);
            String cursorId = parts[1];

            feeds = feedRepository.getFeedForUsers(userId,cursorCreatedAt,cursorId,PageRequest.of(0,size));

        }

        List<String> userIds = feeds.stream()
                .map(Feed::getRecipientUserId)
                .toList();

        Feed last = feeds.get(feeds.size()-1);



        RecipientPage data = new RecipientPage(
                userIds,
                last.getCreatedAt() + "|" + last.getId()
        );


        return data;


    }
}
