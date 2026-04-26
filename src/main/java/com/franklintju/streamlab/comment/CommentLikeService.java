package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.common.DistributedLock;
import com.franklintju.streamlab.exceptions.AlreadyLikedException;
import com.franklintju.streamlab.exceptions.CommentNotFoundException;
import com.franklintju.streamlab.exceptions.NotLikedException;
import com.franklintju.streamlab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final CommentStatsRedisService commentStatsRedisService;

    @DistributedLock(key = "comment:like:#commentId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        CommentLikeId id = new CommentLikeId(userId, commentId);
        if (commentLikeRepository.existsById(id)) {
            throw new AlreadyLikedException();
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        CommentLike like = new CommentLike();
        like.setId(id);
        like.setUser(userRepository.findById(userId).orElseThrow());
        like.setComment(comment);
        commentLikeRepository.save(like);
        commentStatsRedisService.incrementLikes(commentId, 1);

        log.info("User {} liked comment {}", userId, commentId);
    }

    @DistributedLock(key = "comment:like:#commentId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        CommentLikeId id = new CommentLikeId(userId, commentId);
        if (!commentLikeRepository.existsById(id)) {
            throw new NotLikedException();
        }

        commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        commentLikeRepository.deleteById(id);
        commentStatsRedisService.incrementLikes(commentId, -1);

        log.info("User {} unliked comment {}", userId, commentId);
    }
}
