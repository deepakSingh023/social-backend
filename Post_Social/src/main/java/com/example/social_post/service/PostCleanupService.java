package com.example.social_post.service;


import com.example.social_post.dto.ReelUpdate;
import com.example.social_post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PostCleanupService {

    private final S3Service s3Service;
    private final ProfileServiceUpdate profileServiceUpdate;
    private final DeleteFeedService deleteFeedService;
    private final LikesAndCommentService likesAndCommentService;

    @Async("cleanupExecutor")
    public void cleanupPost(
            Post post,
            String userId
    ) {

        deletePostMedia(post);

        profileServiceUpdate.denormProfile(
                new ReelUpdate(userId, -1)
        );

        deleteFeedService.deleteFeed(
                post.getId()
        );

        likesAndCommentService.removeLikesAndComment(
                post.getId()
        );
    }

    private void deletePostMedia(Post post) {

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        if (post.getImageUrls() != null) {

            for (String url : post.getImageUrls()) {

                futures.add(
                        s3Service.deleteFileAsync(url)
                );
            }
        }

        if (post.getVideoUrl() != null &&
                !post.getVideoUrl().isBlank()) {

            futures.add(
                    s3Service.deleteFileAsync(
                            post.getVideoUrl()
                    )
            );
        }

        CompletableFuture.allOf(
                futures.toArray(
                        new CompletableFuture[0]
                )
        ).join();
    }
}