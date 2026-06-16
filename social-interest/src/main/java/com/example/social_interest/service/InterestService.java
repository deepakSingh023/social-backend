package com.example.social_interest.service;

import com.example.social_interest.dto.InterestDto;
import com.example.social_interest.dto.InterestRequest;

import java.util.List;

public interface InterestService {
    void updateInterest(String userId, InterestRequest request);

    InterestDto getInterest(String userId);
}
