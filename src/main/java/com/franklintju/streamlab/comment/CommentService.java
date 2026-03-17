package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.config.RedisConfig;
import com.franklintju.streamlab.exceptions.CommentNotFoundException;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final CommentStatsRedisService commentStatsRedisService;

    @Transactional
    public Comment createComment(Long userId, Long videoId, String content, Long parentId, Long rootId) {
        User user = userRepository.findById(userId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVideo(video);
        comment.setContent(content);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId).orElseThrow();
            comment.setParent(parent);
            if (rootId != null) {
                Comment root = commentRepository.findById(rootId).orElseThrow();
                comment.setRoot(root);
            } else {
                comment.setRoot(parent);
            }
            commentRepository.updateReplyCount(parentId, 1);
        }

        Comment saved = commentRepository.save(comment);

        evictVideoCommentsCache(videoId);

        return saved;
    }

    @Transactional
    public Comment updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        evictVideoCommentsCache(comment.getVideo().getId());

        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        Long videoId = comment.getVideo().getId();

        if (comment.getParent() != null) {
            commentRepository.updateReplyCount(comment.getParent().getId(), -1);
        }
        commentRepository.delete(comment);

        evictVideoCommentsCache(videoId);
    }

    @Cacheable(value = RedisConfig.CACHE_VIDEO_COMMENTS, key = "#videoId + '_' + #pageable.pageNumber")
    public Page<Comment> getVideoComments(Long videoId, Pageable pageable) {
        return commentRepository.findByVideoIdAndRootIsNullAndStatus(videoId, Comment.CommentStatus.VISIBLE, pageable);
    }

    @Cacheable(value = RedisConfig.CACHE_COMMENT_REPLIES, key = "#rootId + '_' + #pageable.pageNumber")
    public Page<Comment> getReplies(Long rootId, Pageable pageable) {
        return commentRepository.findByRootIdAndStatus(rootId, Comment.CommentStatus.VISIBLE, pageable);
    }

    @Cacheable(value = RedisConfig.CACHE_USER_COMMENTS, key = "#userId + '_' + #pageable.pageNumber")
    public Page<Comment> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    @Cacheable(value = RedisConfig.CACHE_COMMENT_COUNT, key = "#videoId")
    public long getCommentCount(Long videoId) {
        return commentRepository.countByVideoIdAndStatus(videoId, Comment.CommentStatus.VISIBLE);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_COMMENT_COUNT, key = "#commentId")
    public void likeComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        commentStatsRedisService.incrementLikes(commentId, 1);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_COMMENT_COUNT, key = "#commentId")
    public void unlikeComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        commentStatsRedisService.incrementLikes(commentId, -1);
    }

    // 手动清除缓存（用于复杂场景）
    @CacheEvict(value = {RedisConfig.CACHE_VIDEO_COMMENTS, RedisConfig.CACHE_COMMENT_COUNT}, key = "#videoId")
    public void evictVideoCommentsCache(Long videoId) {
        // 缓存自动清除
    }
}
