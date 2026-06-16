package com.example.social_post.service;
import com.example.social_post.dto.*;
import com.example.social_post.entity.Post;
import com.example.social_post.exceptions.PostNotFound;
import com.example.social_post.repository.PostRepository;
import com.example.social_post.util.*;
import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final FeedAsyncService feedAsyncService;

    private final ProfileClient profileClient;

    private final  DeleteFeedService deleteFeedService;

     private final LikeClient likeClient;

     private final static Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    @Value("${service.secret}")
    private String token;


    //old creat api with app the things in the backend while new one implement a faster api
    @Override
    public Post createPost(String userId, PostCreation dto) throws Exception {

        List<CompletableFuture<String>> imageFutures = new ArrayList<>();
        String videoUrl = null;

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {

            if (dto.getImages().size() > 7) {
                throw new IllegalArgumentException("Max 7 images allowed");
            }

            for (MultipartFile img : dto.getImages()) {

                if (img.isEmpty()) continue;

                String contentType = img.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid image file");
                }

                byte[] compressedImg = ImageCompressor.compress(img.getBytes());

                imageFutures.add(
                        s3Service.uploadBytesAsync(
                                compressedImg,
                                img.getOriginalFilename(),
                                contentType
                        )
                );
            }
        }

        // ================= VIDEO =================
        if (dto.getVideo() != null && !dto.getVideo().isEmpty()) {

            String contentType = dto.getVideo().getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("Invalid video file");
            }

            long durationSec = extractVideoDuration(dto.getVideo());
            if (durationSec > 40) {
                throw new IllegalArgumentException("Video must be <= 40 seconds");
            }

            byte[] compressedVideo = VideoCompressor.compress(dto.getVideo());

            videoUrl = s3Service.uploadBytesAsync(
                    compressedVideo,
                    dto.getVideo().getOriginalFilename(),
                    "video/mp4"
            ).get(); // wait ONLY for video
        }

        // ================= WAIT FOR IMAGES =================
        List<String> imageUrls = imageFutures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException("Image upload failed", e);
                    }
                })
                .toList();



        InternalProfile profile = profileClient.getInternalData(token,userId);

        // ================= SAVE POST =================
        Post post = Post.builder()
                .userId(userId)
                .caption(dto.getCaption())
                .imageUrls(imageUrls)
                .videoUrl(videoUrl)
                .username(profile.username())
                .avatar(profile.avatar())
                .songUrl(dto.getSongUrl())
                .songName(dto.getSongName())
                .artistName(dto.getArtistName())
                .tags(dto.getTags())
                .isPrivate(dto.isPrivate())
                .createdAt(Instant.now())
                .build();

        CreateFeed data = new CreateFeed(
                userId,
                post.getId()
        );

        feedAsyncService.createFeed(data,token);

        profileClient.updatePostCounter(token,new ReelUpdate(userId,+1));

        return postRepository.save(post);
    }

    @Override
    public Post newCreateApi(String userId, CreatePost data){

        InternalProfile profile = profileClient.getInternalData(token,userId);

        Post post = Post.builder()
                .userId(userId)
                .caption(data.caption())
                .imageUrls(data.images())
                .videoUrl(data.video())
                .username(profile.username())
                .avatar(profile.avatar())
                .songUrl(data.songUrl())
                .songName(data.songName())
                .artistName(data.artistName())
                .tags(data.tags())
                .isPrivate(data.isPrivate())
                .createdAt(Instant.now())
                .build();

        CreateFeed data2 = new CreateFeed(
                userId,
                post.getId()
        );

        feedAsyncService.createFeed(data2,token);

        profileClient.updatePostCounter(token,new ReelUpdate(userId,+1));

        return postRepository.save(post);

    }


    @Override
    public void deletePost(String userId, String postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        if (post.getImageUrls() != null) {
            for (String url : post.getImageUrls()) {
                futures.add(s3Service.deleteFileAsync(url));
            }
        }

        if (post.getVideoUrl() != null) {
            futures.add(s3Service.deleteFileAsync(post.getVideoUrl()));
        }

        // Wait for all deletions
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        profileClient.updatePostCounter(token,new ReelUpdate(userId,-1));

        postRepository.deleteById(postId);

        deleteFeedService.deleteFeed(postId);
    }

    @Override
    public PersonalPosts getPostsByUserId(String profileUserId, String viewerUserId, String cursor) {

        List<Post> posts;

        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findByUserIdOrderByCreatedAtDesc(
                    profileUserId,
                    Pageable.ofSize(10)
            );
        } else {
            Instant instCursor = Instant.parse(cursor);

            posts = postRepository.findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
                    profileUserId,
                    instCursor,
                    Pageable.ofSize(10)
            );
        }

        //  next cursor
        Instant nextCursor = posts.isEmpty()
                ? null
                : posts.get(posts.size() - 1).getCreatedAt();

        //  isOwner (same for all posts)
        boolean isOwner = viewerUserId != null && viewerUserId.equals(profileUserId);

        // collect postIds
        List<String> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        //  call like service (bulk)
        Map<String, Boolean> likedMap= Collections.emptyMap();

        try{
            if (viewerUserId != null && !postIds.isEmpty()) {
                likedMap = likeClient.getLikedStatus(token,viewerUserId, postIds);
            } else {
                likedMap = Collections.emptyMap();
            }
        }catch (Exception e){
            log.error("the likes service is down or not responding",e);
        }

        Map<String, Boolean> likeData = likedMap;


        //  map to DTO
        List<PostResponseDto> postDtos = posts.stream()
                .map(post -> new PostResponseDto(
                        post.getId(),
                        post.getUserId(),
                        post.getAvatar(),
                        post.getUsername(),
                        post.getImageUrls(),
                        post.getVideoUrl(),
                        post.getCaption(),
                        post.getSongUrl(),
                        post.getSongName(),
                        post.getArtistName(),
                        post.getTags(),
                        post.isPrivate(),
                        post.getCreatedAt(),
                        post.getLikes(),
                        post.getComments(),
                        likeData.getOrDefault(post.getId(), false)
                ))
                .toList();

        return new PersonalPosts(postDtos, nextCursor, isOwner);
    }

    @Override
    public List<Post> getPosts(List<String> postIds){
        return postRepository.findAllById(postIds);
    }


    @Override
    public RecipientsPosts getFeedPosts(String authorId, String cursor, int size) {

        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                    authorId,
                    PageRequest.of(0, size)
            );
        } else {
            String[] parts = cursor.split("\\|");
            Instant cursorCreatedAt = Instant.parse(parts[0]);
            String cursorId = parts[1];

            posts = postRepository.findNextPostsByAuthorId(
                    authorId,
                    cursorCreatedAt,
                    cursorId,
                    PageRequest.of(0, size)
            );
        }

        List<String> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        String nextCursor = null;

        if (!posts.isEmpty()) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = last.getCreatedAt() + "|" + last.getId();
        }

        return new RecipientsPosts(postIds, nextCursor);
    }

    @Override
    public IndividualResponse getIndividualPost(String postId,String userId){

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new PostNotFound("this post does not exist"));


        boolean isOwner = post.getUserId().equals(userId);

        boolean isLiked = false;

        try{
            isLiked = likeClient.getIndividualLiked(token, userId, postId);

        }catch (Exception e){
            log.error("likes service is down or not responding",e);
        }

        PostResponseDto res = new PostResponseDto(
                post.getId(),
                post.getUserId(),
                post.getAvatar(),
                post.getUsername(),
                post.getImageUrls(),
                post.getVideoUrl(),
                post.getCaption(),
                post.getSongUrl(),
                post.getSongName(),
                post.getArtistName(),
                post.getTags(),
                post.isPrivate(),
                post.getCreatedAt(),
                post.getLikes(),
                post.getComments(),
                isLiked
        );


        return new IndividualResponse(res,isOwner);

    }


    private long extractVideoDuration(MultipartFile video) throws Exception {
        try (InputStream is = video.getInputStream()) {
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(is, new BodyContentHandler(), metadata);

            String dur = metadata.get("xmpDM:duration");
            if (dur == null) return 0;
            return Math.round(Double.parseDouble(dur) / 1000);
        }
    }
}

