package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.common.RedisLockService;
import com.franklintju.streamlab.config.KafkaConfig;
import com.franklintju.streamlab.interaction.LikeEventMessage.EventType;
import com.franklintju.streamlab.interaction.NotificationMessage.NotificationType;
import com.franklintju.streamlab.users.Profile;
import com.franklintju.streamlab.users.ProfileRepository;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
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
public class LikeEventConsumer {

    private final VideoLikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RedisLockService redisLockService;

    private static final String LIKE_LOCK_KEY = "video:like:consumer";
    private static final int LOCK_EXPIRE_SECONDS = 10;

    @KafkaListener(topics = KafkaConfig.LIKE_EVENT_TOPIC,
                   groupId = "like-event-group",
                   containerFactory = "likeEventConsumerFactory")
    public void handleLikeEvent(String message) {
        try {
            LikeEventMessage event = objectMapper.readValue(message, LikeEventMessage.class);
            log.info("收到点赞事件: userId={}, videoId={}, eventType={}",
                    event.getUserId(), event.getVideoId(), event.getEventType());

            if (event.getEventType() == EventType.LIKE) {
                persistLike(event);
                sendLikeNotification(event);
            } else {
                persistUnlike(event);
            }
        } catch (Exception e) {
            log.error("点赞事件处理失败: {}", message, e);
        }
    }

    @Transactional
    public void persistLike(LikeEventMessage event) {
        String lockKey = LIKE_LOCK_KEY + ":" + event.getVideoId();
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);
        if (lockValue == null) {
            log.warn("获取锁失败，跳过点赞落库: videoId={}", event.getVideoId());
            return;
        }
        try {
            VideoLikeId likeId = new VideoLikeId(event.getUserId(), event.getVideoId());
            if (!likeRepository.existsById(likeId)) {
                Video video = videoRepository.findById(event.getVideoId()).orElse(null);
                User user = userRepository.findById(event.getUserId()).orElse(null);
                if (video != null && user != null) {
                    VideoLike like = new VideoLike();
                    like.setId(likeId);
                    like.setVideo(video);
                    like.setUser(user);
                    likeRepository.save(like);
                    log.info("点赞落库成功: userId={}, videoId={}", event.getUserId(), event.getVideoId());
                }
            } else {
                log.warn("点赞记录已存在，跳过: userId={}, videoId={}", event.getUserId(), event.getVideoId());
            }
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional
    public void persistUnlike(LikeEventMessage event) {
        String lockKey = LIKE_LOCK_KEY + ":" + event.getVideoId();
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);
        if (lockValue == null) {
            log.warn("获取锁失败，跳过取消点赞落库: videoId={}", event.getVideoId());
            return;
        }
        try {
            VideoLikeId likeId = new VideoLikeId(event.getUserId(), event.getVideoId());
            if (likeRepository.existsById(likeId)) {
                likeRepository.deleteById(likeId);
                log.info("取消点赞落库成功: userId={}, videoId={}", event.getUserId(), event.getVideoId());
            } else {
                log.warn("点赞记录不存在，跳过: userId={}, videoId={}", event.getUserId(), event.getVideoId());
            }
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    private void sendLikeNotification(LikeEventMessage event) {
        if (event.getUserId().equals(event.getVideoOwnerId())) {
            return;
        }
        try {
            Profile profile = profileRepository.findByUserId(event.getUserId()).orElse(null);
            String senderName = profile != null ? profile.getUsername() : "某用户";

            NotificationMessage notification = NotificationMessage.of(
                    event.getVideoOwnerId(),
                    NotificationType.LIKE,
                    event.getVideoId(),
                    event.getUserId(),
                    senderName + " 点赞了你的视频"
            );

            String jsonMessage = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send(KafkaConfig.NOTIFICATION_TOPIC, jsonMessage);
            log.info("发送点赞通知成功: receiverId={}", event.getVideoOwnerId());
        } catch (Exception e) {
            log.error("发送点赞通知失败: receiverId={}", event.getVideoOwnerId(), e);
        }
    }
}
