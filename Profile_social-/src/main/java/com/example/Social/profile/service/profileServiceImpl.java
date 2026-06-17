package com.example.Social.profile.service;

import com.example.Social.profile.dto.*;
import com.example.Social.profile.entity.profile;
import com.example.Social.profile.exceptions.ProfileNotFound;
import com.example.Social.profile.repository.ProfileRepository;

import com.example.Social.profile.tasks.InteractionClient;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class profileServiceImpl implements profileService {

    private final ProfileRepository profileRepository;
    private final R2ImageService r2ImageService;
    private final MongoTemplate mongoTemplate;
    private final DenormalizeService denormalizeService;

    private final InteractionClient interactionClient;

    public  static final Logger log = LoggerFactory.getLogger(profileServiceImpl.class);


    @Value("${secret.service}")
    private String token;





    @Override
    public profile fetchOrCreateProfile(createProfile data) {

        return profileRepository.findByUserId(data.getUserId())
                .orElseGet(() -> {
                    try {
                        profile p = new profile();
                        p.setUserId(data.getUserId());
                        p.setUsername(data.getUsername());
                        p.setEmail(data.getEmail());
                        return profileRepository.save(p);
                    } catch (DuplicateKeyException e) {
                        return profileRepository.findByUserId(data.getUserId())
                                .orElseThrow();
                    }
                });
    }

    @Transactional
    public void updateAvatar(
            String userId,
            String avatarUrl
    ) {

        profile profile = profileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Profile not found"));

        String oldAvatar = profile.getProfilePicUrl();

        profile.setProfilePicUrl(avatarUrl);

        profileRepository.save(profile);

        denormalizeService.denormalize(
                new DenormalizeDto(
                        userId,
                        avatarUrl
                )
        );

        if(oldAvatar != null && !oldAvatar.isBlank()){
            denormalizeService.deleteOldImageAsync(oldAvatar);
        }
    }



    @Transactional
    public profile updateProfile(String userId, updateProfile data, MultipartFile newPic) {
        long start = System.currentTimeMillis();

        long t1 = System.currentTimeMillis();
        profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFound("Profile not found"));
        log.info("find profile took {} ms", System.currentTimeMillis() - t1);

        if (data.getBio() != null) profile.setBio(data.getBio());
        if (data.getPrivateAcc() != null) profile.setPrivateAcc(data.getPrivateAcc());

        String oldUrl = profile.getProfilePicUrl();

        if (newPic != null && !newPic.isEmpty()) {
            long t2 = System.currentTimeMillis();
            String newUrl = r2ImageService.uploadProfilePic(newPic);
            log.info("uploadProfilePic took {} ms", System.currentTimeMillis() - t2);
            profile.setProfilePicUrl(newUrl);
        }

        long t3 = System.currentTimeMillis();
        profile saved = profileRepository.save(profile);
        log.info("save profile took {} ms", System.currentTimeMillis() - t3);

        if (newPic != null && !newPic.isEmpty()) {
            DenormalizeDto denorm = new DenormalizeDto(userId, saved.getProfilePicUrl());
            denormalizeService.denormalize(denorm);

            if (oldUrl != null && !oldUrl.isEmpty()) {
                try {
                    denormalizeService.deleteOldImageAsync(oldUrl);
                } catch (Exception e) {
                    log.warn("Failed to delete old image oldUrl={}", oldUrl, e);
                }
            }
        }

        log.info("total updateProfile took {} ms", System.currentTimeMillis() - start);
        return saved;
    }


    @Override
    public fetchProfile getProfile(String userId) {

        profile data = profileRepository.findByUserId(userId)
                .orElseThrow(()-> new ProfileNotFound("profile not found"));

        fetchProfile res = new fetchProfile(
                userId,
                data.getUsername(),
                data.getBio(),
                data.getEmail(),
                data.getProfilePicUrl(),
                data.getPrivateAcc(),
                data.getReels(),
                data.getPosts(),
                data.getFollowerCounter(),
                data.getFollowingCounter(),
                data.getFriendsCounter()
        );

        return res;
    }

    @Override
    public FetchSomeoneProfile fetchSomeoneElseProfile(String userId, String profileOwnerId){

        profile pro = profileRepository.findByUserId(profileOwnerId)
                .orElseThrow(()-> new ProfileNotFound("profile not found"));

        fetchProfile res = new fetchProfile(
                pro.getUserId(),
                pro.getUsername(),
                pro.getBio(),
                pro.getEmail(),
                pro.getProfilePicUrl(),
                pro.getPrivateAcc(),
                pro.getReels(),
                pro.getPosts(),
                pro.getFollowerCounter(),
                pro.getFollowingCounter(),
                pro.getFriendsCounter()
        );

        InteractionResponse res2;

        try {
            res2 = interactionClient.checkInteraction(
                    new CheckInteraction(userId, profileOwnerId),
                    token
            );
        } catch (Exception e) {

            // optional log
            log.error("Interaction service failed", e);

            res2 = new InteractionResponse(false, false);
        }

        return new FetchSomeoneProfile(res,res2);

    }

    @Override
    public InternalProfile getInternal(String userId){
       profile pro =  profileRepository.findByUserId(userId)
               .orElseThrow(() -> new ProfileNotFound("this profile does not exist"));

       return new InternalProfile(pro.getUsername(),pro.getProfilePicUrl());
    }
}
