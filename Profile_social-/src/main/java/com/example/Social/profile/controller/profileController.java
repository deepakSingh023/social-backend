package com.example.Social.profile.controller;

import com.example.Social.profile.dto.*;
import com.example.Social.profile.entity.profile;
import com.example.Social.profile.service.ProfileDataFetchService;
import com.example.Social.profile.service.profileService;


import lombok.RequiredArgsConstructor;
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

    @PostMapping("/create")
    public ResponseEntity<?> fetchOrCreateProfile(
            @RequestBody createProfile request
    ) {
        profile profile = profileService.fetchOrCreateProfile(request);

        return ResponseEntity.ok(profile);
    }


    // UPDATE profile fields (JWT required)
    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") updateProfile request,
            @RequestPart(value = "profilePic", required = false) MultipartFile profilePic,
            Authentication auth
    ) {

        String userId = auth.getName();
        profile profile = profileService.updateProfile(userId, request, profilePic);
        return ResponseEntity.ok(profile);
    }




    @PostMapping("/fetch-profile")
    public ResponseEntity<fetchProfile> fetch(
            Authentication auth
    ){

        String userId = auth.getName();

        fetchProfile data = profileService.getProfile(userId);

        return ResponseEntity.ok(data);

    }

    @GetMapping("/fetch-profile-else/{otherUserId}")
    public ResponseEntity<FetchSomeoneProfile> fetchElse(
            @PathVariable String otherUserId,
            Authentication auth
    ){
        FetchSomeoneProfile res = profileService.fetchSomeoneElseProfile(auth.getName(), otherUserId);

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
