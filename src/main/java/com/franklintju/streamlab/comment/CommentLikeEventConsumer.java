package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.common.RedisLockService;
import com.franklintju.streamlab.config.KafkaConfig;
import com.franklintju.streamlab.interaction.NotificationMessage;
import com.franklintju.streamlab.interaction.NotificationMessage.NotificationType;
import com.franklintju.streamlab.users.ProfileRepository;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeEventConsumer {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RedisLockService redisLockService;

    private static final String LOCK_KEY = "comment:like:consumer";
    private static final int LOCK_EXPIRE = 10;

    @KafkaListener(topics = KafkaConfig.COMMENT_LIKE_EVENT_TOPIC,
                   groupId = "comment-like-group",
                   containerFactory = "commentLikeEventConsumerFactory")
    public void handleCommentLikeEvent(String message) {
        try {
            CommentLikeEventMessage event = objectMapper.readValue(message, CommentLikeEventMessage.class);
            log.info("收到评论点赞事件: userId={}, commentId={}, eventType={}",
                    event.getUserId(), event.getCommentId(), event.getEventType());

            if (event.getEventType() == CommentLikeEventMessage.EventType.LIKE) {
                persistCommentLike(event);
                sendNotification(event);
            } else {
                persistCommentUnlike(event);
            }
        } catch (Exception e) {
            log.error("评论点赞事件处理失败: {}", message, e);
        }
    }

    @Transactional
    public void persistCommentLike(CommentLikeEventMessage event) {
        String lockValue = redisLockService.acquireLock(LOCK_KEY + ":" + event.getCommentId(), LOCK_EXPIRE);
        if (lockValue == null) {
            log.warn("获取锁失败，跳过评论点赞落库: commentId={}", event.getCommentId());
            return;
        }
        try {
            CommentLikeId id = new CommentLikeId(event.getUserId(), event.getCommentId());
            if (!commentLikeRepository.existsById(id)) {
                User user = userRepository.findById(event.getUserId()).orElse(null);
                Comment comment = commentRepository.findById(event.getCommentId()).orElse(null);
                if (user != null && comment != null) {
                    CommentLike like = new CommentLike();
                    like.setId(id);
                    like.setUser(user);
                    like.setComment(comment);
                    commentLikeRepository.save(like);
                    log.info("评论点赞落库成功: userId={}, commentId={}", event.getUserId(), event.getCommentId());
                }
            }
        } finally {
            redisLockService.releaseLock(LOCK_KEY + ":" + event.getCommentId(), lockValue);
        }
    }

    @Transactional
    public void persistCommentUnlike(CommentLikeEventMessage event) {
        String lockValue = redisLockService.acquireLock(LOCK_KEY + ":" + event.getCommentId(), LOCK_EXPIRE);
        if (lockValue == null) {
            log.warn("获取锁失败，跳过评论取消点赞落库: commentId={}", event.getCommentId());
            return;
        }
        try {
            CommentLikeId id = new CommentLikeId(event.getUserId(), event.getCommentId());
            if (commentLikeRepository.existsById(id)) {
                commentLikeRepository.deleteById(id);
                log.info("评论取消点赞落库成功: userId={}, commentId={}", event.getUserId(), event.getCommentId());
            }
        } finally {
            redisLockService.releaseLock(LOCK_KEY + ":" + event.getCommentId(), lockValue);
        }
    }

    private void sendNotification(CommentLikeEventMessage event) {
        if (event.getUserId().equals(event.getCommentOwnerId())) return;
        try {
            String senderName = profileRepository.findByUserId(event.getUserId())
                    .map(p -> p.getUsername()).orElse("某用户");
            NotificationMessage notification = NotificationMessage.of(
                    event.getCommentOwnerId(),
                    NotificationType.COMMENT,
                    event.getCommentId(),
                    event.getUserId(),
                    senderName + " 点赞了你的评论"
            );
            String json = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send(KafkaConfig.NOTIFICATION_TOPIC, json);
        } catch (Exception e) {
            log.error("发送评论点赞通知失败: receiverId={}", event.getCommentOwnerId(), e);
        }
    }
}
