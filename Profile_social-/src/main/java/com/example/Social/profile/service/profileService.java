package com.example.Social.profile.service;

import com.example.Social.profile.dto.*;
import com.example.Social.profile.entity.profile;
import org.springframework.web.multipart.MultipartFile;

public interface profileService {

    profile updateProfile(String userId, updateProfile data, MultipartFile newPic);

    profile fetchOrCreateProfile(createProfile data);

    FetchSomeoneProfile fetchSomeoneElseProfile(String userId, String profileOwnerId);

    fetchProfile getProfile(String userId);

    InternalProfile getInternal(String userId);

    void updateAvatar(
            String userId,
            String avatarUrl
    );


}
