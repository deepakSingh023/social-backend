package com.example.social_likes.service;


import com.example.social_likes.repository.CommentsRepository;
import com.example.social_likes.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class LikeAndCommentCleanupService {

    private final LikesRepository likesRepository;

    private final CommentsRepository commentsRepository;

    @Async
    public void deleteLikesAndComment(String postId){

        likesRepository.deleteAllByTargetId(postId);
        commentsRepository.deleteAllByPostId(postId);

    }


    @Async
    public void deleteReplies(String targetId){
        commentsRepository.deleteAllByParentCommentId(targetId);
        likesRepository.deleteAllByTargetId(targetId);

    }
}
