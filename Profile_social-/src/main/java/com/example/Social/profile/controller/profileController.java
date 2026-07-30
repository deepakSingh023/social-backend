package com.example.Social.profile.controller;

import com.example.Social.profile.dto.*;
import com.example.Social.profile.entity.profile;
import com.example.Social.profile.service.PresignedUrlService;
import com.example.Social.profile.service.ProfileDataFetchService;
import com.example.Social.profile.service.profileService;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")   // All routes start with /api/profiles
@RequiredArgsConstructor
public class profileController {

    private final profileService profileService;

    private final ProfileDataFetchService profileDataFetchService;

    private final PresignedUrlService presignedUrlService;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;


    @PostMapping("/create")
    public ResponseEntity<?> fetchOrCreateProfile(
            @RequestBody createProfile request
    ) {
        profile profile = profileService.fetchOrCreateProfile(request);

        return ResponseEntity.ok(profile);
    }


    @PostMapping("/upload-url")
    public UploadResponse getUploadUrl(@RequestBody UploadRequest req,
                                       @RequestHeader("X-User-Id") String userId) {


        if (req.contentType() == null ||
                (!req.contentType().startsWith("image/") && !req.contentType().startsWith("video/"))) {
            throw new IllegalArgumentException("Only image or video uploads allowed");
        }

        // 🔒 Sanitize filename
        String safeFileName = req.fileName().replaceAll("[^a-zA-Z0-9.-]", "_");

        String key = userId + "/" + System.currentTimeMillis() + "-" + safeFileName;

        String uploadUrl = presignedUrlService.generatePresignedUrl(key, req.contentType());

        String fileUrl = publicBaseUrl + "/" + key;

        return new UploadResponse(uploadUrl, fileUrl);
    }



    @PostMapping("/avatar")
    public ResponseEntity<Void> updateAvatar(
            @RequestBody UpdateAvatarRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {


        profileService.updateAvatar(
                userId,
                request.avatarUrl()
        );

        return ResponseEntity.ok().build();
    }

    // UPDATE profile fields (JWT required)
    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") updateProfile request,
            @RequestPart(value = "profilePic", required = false) MultipartFile profilePic,
            @RequestHeader("X-User-Id") String userId
    ) {

        profile profile = profileService.updateProfile(userId, request, profilePic);
        return ResponseEntity.ok(profile);
    }




    @PostMapping("/fetch-profile")
    public ResponseEntity<fetchProfile> fetch(
            @RequestHeader("X-User-Id") String userId
    ){


        fetchProfile data = profileService.getProfile(userId);

        return ResponseEntity.ok(data);

    }

    @GetMapping("/fetch-profile-else/{otherUserId}")
    public ResponseEntity<FetchSomeoneProfile> fetchElse(
            @PathVariable String otherUserId,
            @RequestHeader("X-User-Id") String userId
    ){
        FetchSomeoneProfile res = profileService.fetchSomeoneElseProfile(userId, otherUserId);

        return ResponseEntity.ok(res);
    }


    @GetMapping("/get/profile-stuff/{userId}")
    public ResponseEntity<InternalProfile> getInternalData(

            @PathVariable String userId

    ){

        InternalProfile res = profileService.getInternal(userId);

        return ResponseEntity.ok(res);
    }


    @PostMapping("/get/basic")
    public ResponseEntity<Map<String, ProfileDto>> getProfiles(@RequestBody List<String> ids) {
        return ResponseEntity.ok(profileDataFetchService.getProfiles(ids));
    }







}
