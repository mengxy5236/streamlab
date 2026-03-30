package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.common.DistributedLock;
import com.franklintju.streamlab.exceptions.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeEventProducer commentLikeEventProducer;

    @DistributedLock(key = "comment:like:#commentId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    public void likeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        CommentLikeEventMessage event = CommentLikeEventMessage.like(
                userId,
                commentId,
                comment.getUser().getId(),
                comment.getVideo().getId()
        );
        commentLikeEventProducer.sendCommentLikeEvent(event);

        log.info("User {} liked comment {} (Kafka消息已发送)", userId, commentId);
    }

    @DistributedLock(key = "comment:like:#commentId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    public void unlikeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        CommentLikeEventMessage event = CommentLikeEventMessage.unlike(
                userId,
                commentId,
                comment.getUser().getId(),
                comment.getVideo().getId()
        );
        commentLikeEventProducer.sendCommentLikeEvent(event);

        log.info("User {} unliked comment {} (Kafka消息已发送)", userId, commentId);
    }
}
