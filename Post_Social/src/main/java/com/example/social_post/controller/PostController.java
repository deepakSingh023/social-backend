package com.example.social_post.controller;

import com.example.social_post.dto.*;
import com.example.social_post.entity.Post;
import com.example.social_post.service.PostService;
import com.example.social_post.service.PresignedUrlService;
import com.example.social_post.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final PresignedUrlService presignedUrlService;

    @Value("${cloudflare.r2.posts-public-url}")
    private String publicBaseUrl;

    // ---------------- CREATE POST ----------------

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<Post> createPost(
            Authentication authentication,
            @ModelAttribute PostCreation postCreation
    ) throws Exception {

        String userId = authentication.getName();
        Post post = postService.createPost(userId, postCreation);

        return ResponseEntity.ok(post);
    }



    //create post with the new api of upload first then save


    @PostMapping("/frontend-upload/create")
    public ResponseEntity<Post> createPostNew(
            Authentication authentication,
            @RequestBody CreatePost createPost
    ){

        String userId = authentication.getName();

        Post post = postService.newCreateApi(userId,createPost);

        return  ResponseEntity.ok(post);

    }

    // ---------------- DELETE POST ----------------

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            Authentication authentication,
            @PathVariable String postId
    ) {

        String userId = authentication.getName(); // owner check
        postService.deletePost(userId, postId);

        return ResponseEntity.ok("Post deleted successfully");
    }

    //this is to get the posts of the user of by another user to see the post of another user
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<PersonalPosts> getUserPosts(
            @PathVariable String userId,
            @RequestParam(required = false) String cursor,
            Authentication authentication) {

        String viewerUserId = authentication != null ? authentication.getName() : null;

        PersonalPosts response = postService.getPostsByUserId(userId, viewerUserId, cursor);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/posts/{postId}")
    public ResponseEntity<IndividualResponse> getIndividualPost(
            @PathVariable String postId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        IndividualResponse res = postService.getIndividualPost(postId, userId);
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


}