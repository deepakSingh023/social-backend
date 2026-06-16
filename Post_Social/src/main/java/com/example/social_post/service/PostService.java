package com.example.social_post.service;
import com.example.social_post.dto.*;
import com.example.social_post.entity.Post;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostService {
    Post createPost(String userId, PostCreation dto) throws Exception;

    void deletePost(String userId, String postId);

    PersonalPosts getPostsByUserId(String profileUserId, String viewerUserId, String cursor);

    List<Post> getPosts(List<String> postIds);

    RecipientsPosts getFeedPosts(String authorId, String cursor, int size);

    IndividualResponse getIndividualPost(String postId, String userId);

    Post newCreateApi(String userId, CreatePost data);

}

