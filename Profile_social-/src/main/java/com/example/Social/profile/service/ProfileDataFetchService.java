package com.example.Social.profile.service;

import com.example.Social.profile.dto.ProfileDto;
import com.example.Social.profile.entity.profile;
import com.example.Social.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class ProfileDataFetchService {


    private final ProfileRepository profileRepository;



    public Map<String, ProfileDto> getProfiles(List<String> ids) {

        List<profile> users = profileRepository.findByUserIdIn(ids);

        return users.stream()
                .collect(Collectors.toMap(
                        profile::getUserId,
                        user -> new ProfileDto(
                                user.getUsername(),
                                user.getProfilePicUrl()
                        )
                ));
    }
}
