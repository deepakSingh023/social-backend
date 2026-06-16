package com.example.social_reel.controller;

import com.example.social_reel.dto.*;
import com.example.social_reel.entity.Reel;
import com.example.social_reel.service.PresignedUrlService;
import com.example.social_reel.service.ReelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reels")
@RequiredArgsConstructor
public class ReelController {

    private final ReelService reelService;

    private final PresignedUrlService presignedUrlService;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    @PostMapping(value = "/create",consumes = "multipart/form-data")
    public ResponseEntity<Reel> createReel(
            @RequestPart("video") MultipartFile video,
            @RequestPart("tags") List<String> tags,
            @RequestPart("caption") String caption,
            Authentication authentication
    ) throws Exception {

        String userId = authentication.getName();
        return ResponseEntity.ok(
                reelService.createReel(userId, video, tags, caption)
        );
    }

    @DeleteMapping("/{reelId}")
    public ResponseEntity<Void> deleteReel(
            @PathVariable String reelId,
            Authentication authentication
    ) {
        reelService.deleteReel(reelId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{postOwnerId}/reels")
    public ResponseEntity<PersonalReels> myReels(
            @PathVariable String postOwnerId,
            @RequestParam(required = false) String cursor,
            Authentication authentication
    ) {

        String userId= authentication.getName();
        return ResponseEntity.ok(
                reelService.getMyReels(userId,postOwnerId,cursor)
        );
    }


    @GetMapping("/users/reels/{reelId}")
    public ResponseEntity<IndividualResponse> getIndividualReel(
            @PathVariable String reelId,
            Authentication authentication
    ){
        String userId = authentication.getName();

        IndividualResponse res = reelService.getReel(userId,reelId);

        return ResponseEntity.ok(res);

    }

    @PostMapping("/upload-url")
    public UploadResponse getUploadUrl(@RequestBody UploadRequest req,
                                       Authentication authentication) {

        String userId = authentication.getName();

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


    @PostMapping("/frontend-upload/create")
    public ResponseEntity<Reel> createPostNew(
            Authentication authentication,
            @RequestBody CreateReel createPost
    ){

        String userId = authentication.getName();

        Reel post = reelService.newCreateApi(userId,createPost);

        return  ResponseEntity.ok(post);

    }

}
