package com.example.social_likes.service;

import com.example.social_likes.dto.CommentResponseDTO;
import com.example.social_likes.dto.CreateCommentDTO;
import com.example.social_likes.dto.IncrementDecDto;
import com.example.social_likes.dto.InternalProfile;
import com.example.social_likes.entity.Comments;
import com.example.social_likes.enums.ImpressionType;
import com.example.social_likes.enums.LikeTargetType;
import com.example.social_likes.repository.CommentsRepository;
import com.example.social_likes.util.ProfileClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;

    private final DenormalizeService denormalizeService;

    private final ProfileClient profileClient;

    private final LikesService likesService;

    private final LikeAndCommentCleanupService likeAndCommentCleanupService;


    private static final Logger log = LoggerFactory.getLogger(CommentsServiceImpl.class);

    @Value("${service.secret}")
    private String secret;


    @Override
    public CommentResponseDTO createComment(CreateCommentDTO data, String userId) {

        if (data.getPostId() == null || data.getContent() == null || data.getContent().isBlank()) {
            throw new IllegalArgumentException("PostId and content required");
        }

        if (data.getParentCommentId() != null &&
                !commentsRepository.existsById(data.getParentCommentId())) {
            throw new IllegalArgumentException("Parent comment not found");
        }

        if(data.getCommentType() == null)
            throw new IllegalArgumentException("Comment type required");

        InternalProfile profile = profileClient.getInternalData(userId,secret);

        Comments comment = Comments.builder()
                .postId(data.getPostId())
                .parentCommentId(data.getParentCommentId())
                .type(data.getCommentType())
                .userId(userId)
                .username(profile.username())
                .userAvatar(profile.avatar())
                .content(data.getContent())
                .likesCount(0)
                .repliesCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Comments saved = commentsRepository.save(comment);

        if(data.getParentCommentId() == null || data.getParentCommentId().isEmpty()){
            denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(comment.getPostId(),ImpressionType.COMMENT,+1),data.getCommentType(),userId);
        }else{
            denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(comment.getParentCommentId(),ImpressionType.COMMENT,+1),LikeTargetType.COMMENT_REPLY,userId);

        }


        return map(saved,userId);
    }

    @Override
    public void deleteComment(String commentId, String userId) {

        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("Not allowed");
        }


        commentsRepository.delete(comment);

        if(comment.getParentCommentId() == null || comment.getParentCommentId().isEmpty()){
            denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(comment.getPostId(),ImpressionType.COMMENT,-1),comment.getType(),userId);
        }else{
            denormalizeService.denormalizeLikeAndCommentCount(new IncrementDecDto(comment.getParentCommentId(),ImpressionType.COMMENT,-1),LikeTargetType.COMMENT_REPLY,userId);

        }

        likeAndCommentCleanupService.deleteReplies(commentId);
    }

    @Override
    public List<CommentResponseDTO> getCommentsByPost(
            String postId,
            String userId,
            String cursor
    ) {


        log.info("postId = {}  userId = {}  cursor = {}",postId,userId,cursor);


        Instant instant = null;



        if(cursor != null && !cursor.isBlank()){
             instant = Instant.parse(cursor);
        }

        List<Comments> comments;

        if(cursor == null || cursor.isBlank()) {

            comments = commentsRepository
                    .findByPostIdAndParentCommentIdIsNull(
                            postId,
                            Pageable.ofSize(10)
                    );

        } else {

            comments = commentsRepository
                    .findByPostIdAndParentCommentIdIsNullAndCreatedAtLessThan(
                            postId,
                            instant,
                            Pageable.ofSize(10)
                    );
        }

        List<String> commentIds = comments.stream()
                .map(Comments::getId).toList();

        Map<String,Boolean> liked = likesService.likedList(userId,commentIds);

        List<CommentResponseDTO> res = comments.stream()
                .map(comment -> {

                    boolean owner =
                            userId != null &&
                                    userId.equals(comment.getUserId());

                    log.info(
                            "requestUser={} commentUser={} owner={}",
                            userId,
                            comment.getUserId(),
                            owner
                    );

                    return new CommentResponseDTO(
                            comment.getId(),
                            comment.getPostId(),
                            comment.getParentCommentId(),
                            comment.getUserId(),
                            comment.getContent(),
                            comment.getUsername(),
                            comment.getUserAvatar(),
                            comment.getLikesCount(),
                            comment.getRepliesCount(),
                            liked.getOrDefault(comment.getId(),false),
                            owner,
                            comment.getCreatedAt()
                    );
                })
                .toList();



        log.info("data in response size={} ",res.size());

        return res;


    }

    @Override
    public List<CommentResponseDTO> getReplies(
            String parentCommentId,
            String userId,
            String cursor
    ) {

        Instant instant = null;

        if(cursor != null && !cursor.isBlank()){
            instant = Instant.parse(cursor);
        }

        List<Comments> comments;

        if(cursor == null || cursor.isBlank()) {

            comments = commentsRepository
                    .findByParentCommentId(
                            parentCommentId,
                            Pageable.ofSize(10)
                    );

        } else {

            comments = commentsRepository
                    .findByParentCommentIdAndCreatedAtLessThan(
                            parentCommentId,
                            instant,
                            Pageable.ofSize(10)
                    );
        }

        List<String> commentIds = comments.stream()
                .map(Comments::getId).toList();

        Map<String,Boolean> liked = likesService.likedList(userId,commentIds);

        List<CommentResponseDTO> res = comments.stream()
                .map(comment -> new CommentResponseDTO(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getParentCommentId(),
                        comment.getUserId(),
                        comment.getContent(),
                        comment.getUsername(),
                        comment.getUserAvatar(),
                        comment.getLikesCount(),
                        comment.getRepliesCount(),
                        liked.getOrDefault(comment.getId(),false),
                        userId != null && userId.equals(comment.getUserId()),
                        comment.getCreatedAt()
                )).toList();


        return res;

    }

    private CommentResponseDTO map(Comments c, String userId) {
        return CommentResponseDTO.builder()
                .id(c.getId())
                .postId(c.getPostId())
                .parentCommentId(c.getParentCommentId())
                .userId(c.getUserId())
                .username(c.getUsername())
                .userAvatar(c.getUserAvatar())
                .content(c.getContent())
                .likesCount(c.getLikesCount())
                .repliesCount(c.getRepliesCount())
                .likedByCurrentUser(likesService.isLiked(c.getUserId(), c.getPostId())) // integrate likes service later
                .isOwner(userId!=null && userId.equals(c.getUserId()))
                .createdAt(c.getCreatedAt())
                .build();
    }
}



