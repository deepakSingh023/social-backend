package com.example.social_view.service;


import com.example.social_view.dto.ViewDto;

public interface ViewService {
    void registerView(ViewDto data , String userId);

    void registerInterestLikes(String reelId, String userId);
}
