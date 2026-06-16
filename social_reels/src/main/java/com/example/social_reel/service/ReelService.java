package com.example.social_reel.service;

import com.example.social_reel.dto.CreateReel;
import com.example.social_reel.dto.IndividualResponse;
import com.example.social_reel.dto.PersonalReels;
import com.example.social_reel.entity.Reel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReelService {

    Reel createReel(
            String userId,
            MultipartFile video,
            List<String> data,
            String caption
    ) throws Exception;

    void deleteReel(String reelId, String userId);

    PersonalReels getMyReels(String userId, String ownerId , String cursor);

    IndividualResponse getReel(String userId, String postId);

    Reel newCreateApi(String userId, CreateReel reel);
}
