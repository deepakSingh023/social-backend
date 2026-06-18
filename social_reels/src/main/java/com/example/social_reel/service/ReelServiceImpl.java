package com.example.social_reel.service;

import com.example.social_reel.dto.*;
import com.example.social_reel.entity.Reel;
import com.example.social_reel.exceptions.ReelNotFound;
import com.example.social_reel.repository.ReelRepository;
import com.example.social_reel.util.InteractionClient;
import com.example.social_reel.util.LikeClient;
import com.example.social_reel.util.ProfileClient;
import com.example.social_reel.util.VideoCompressor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReelServiceImpl implements ReelService {

    private final S3Client s3Client;
    private final ReelRepository reelRepository;
    private final SemanticTagResolver tagResolver;

    private final ProfileClient profileClient;

    private final InteractionClient interactionClient;

    private static final Logger log = LoggerFactory.getLogger(ReelServiceImpl.class);

    private final LikeClient likeClient;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    @Value("${service.secret}")
    private String token;

    @Override
    public Reel createReel(
            String userId,
            MultipartFile video,
            List<String> data,
            String caption
    ) throws Exception {

        validateVideo(video);

        File compressed = VideoCompressor.compressToFile(video);

        String videoKey = "reels/" + UUID.randomUUID() + ".mp4";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(videoKey)
                        .contentType("video/mp4")
                        .build(),
                RequestBody.fromFile(compressed)
        );

        compressed.delete();

        String publicVideoUrl = publicBaseUrl + "/" + videoKey;


        InternalProfile profile = profileClient.getInternalData(token,userId);

        Reel reel = new Reel();
        reel.setUserId(userId);
        reel.setVideoUrl(publicVideoUrl);
        reel.setRawTags(data);
        reel.setCaption(caption);
        reel.setUsername(profile.username());
        reel.setAvatar(profile.avatar());
        reel.setSemanticTags(tagResolver.resolve(data));
        reel.setLikes(0);
        reel.setComments(0);
        reel.setCreatedAt(Instant.now());


        profileClient.updateReelCounter(token,new ReelUpdate(userId,+1));

        return reelRepository.save(reel);
    }

    @Override
    public Reel newCreateApi(String userId, CreateReel data){

        InternalProfile profile = profileClient.getInternalData(token,userId);


        Reel reel = Reel.builder()
                .userId(userId)
                .caption(data.caption())
                .videoUrl(data.videoUrl())
                .username(profile.username())
                .avatar(profile.avatar())
                .rawTags(data.tags())
                .semanticTags(tagResolver.resolve(data.tags()))
                .likes(0)
                .comments(0)
                .createdAt(Instant.now())
                .build();

        profileClient.updateReelCounter(token,new ReelUpdate(userId,+1));

        return reelRepository.save(reel);
    }

    @Override
    public void deleteReel(String reelId, String userId) {

        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new RuntimeException("Reel not found"));

        if (!reel.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        String key = reel.getVideoUrl()
                .replace(publicBaseUrl + "/", "");

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        reelRepository.delete(reel);
        profileClient.updateReelCounter(token,new ReelUpdate(userId,-1));

        interactionClient.deleteLikesAndComments(reelId,token);
    }

    @Override
    public PersonalReels getMyReels(String userId, String ownerId , String cursor) {

        List<Reel> reels;



        if(cursor==null || cursor.isEmpty()){
            reels = reelRepository.findByUserIdOrderByCreatedAtDesc(ownerId, Pageable.ofSize(10));
        }else{
            Instant instantCursor = Instant.parse(cursor);

            reels = reelRepository.findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(ownerId,instantCursor,Pageable.ofSize(10));
        }

        boolean isOwner = userId != null && userId.equals(ownerId);

        Instant newCursor = null;

        if (!reels.isEmpty()) {
            newCursor = reels.get(reels.size() - 1).getCreatedAt();
        }


        List<String> reelIds = reels.stream()
                .map(Reel::getId)
                .toList();


        Map<String, Boolean> tempLikedMap = Collections.emptyMap();

        if (userId != null && !reelIds.isEmpty()) {
            try {
                tempLikedMap = likeClient.getLikedStatus(
                        token,
                        userId,
                        reelIds
                );
            } catch (Exception e) {
                System.out.println("Likes service unavailable");
            }
        }

        final Map<String, Boolean> likedMap = tempLikedMap;


        List<PersonalReelsResponse> res = reels.stream()
                .map(reel->new PersonalReelsResponse(
                        reel.getId(),
                        reel.getUsername(),
                        reel.getAvatar(),
                        reel.getVideoUrl(),
                        reel.getRawTags(),
                        reel.getSemanticTags(),
                        reel.getCaption(),
                        reel.getViewCount(),
                        reel.getLikes(),
                        reel.getComments(),
                        reel.getCreatedAt(),
                        likedMap.getOrDefault(reel.getId(),false)

                )).toList();


        return new PersonalReels(res,newCursor,isOwner);

    }

    @Override
    public IndividualResponse getReel(String userId, String postId){

        Reel reel = reelRepository.findById(postId)
                .orElseThrow(()->new ReelNotFound("this reel does not exit"));


        boolean isLiked = false;

        try {
            isLiked = likeClient.getIndividualLiked(
                    token,
                    userId,
                    postId
            );
        } catch (Exception e) {
            log.error("likes service down failed to fetch likes", e);
        }


        PersonalReelsResponse res = new PersonalReelsResponse(
                reel.getId(),
                reel.getUsername(),
                reel.getAvatar(),
                reel.getVideoUrl(),
                reel.getRawTags(),
                reel.getSemanticTags(),
                reel.getCaption(),
                reel.getViewCount(),
                reel.getLikes(),
                reel.getComments(),
                reel.getCreatedAt(),
                isLiked
        );

        boolean isOwner = userId !=null && userId.equals(reel.getUserId());

        return new IndividualResponse(res,isOwner);

    }

    private void validateVideo(MultipartFile video) {

        if (video.isEmpty())
            throw new IllegalArgumentException("Video is empty");

        if (video.getContentType() == null ||
                !video.getContentType().startsWith("video/"))
            throw new IllegalArgumentException("Invalid video type");

        if (video.getSize() > 60 * 1024 * 1024)
            throw new IllegalArgumentException("Video too large");
    }
}
