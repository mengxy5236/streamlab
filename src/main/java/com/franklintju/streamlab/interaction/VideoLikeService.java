package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.common.RedisLockService;
import com.franklintju.streamlab.exceptions.AlreadyLikedException;
import com.franklintju.streamlab.exceptions.NotLikedException;
import com.franklintju.streamlab.exceptions.VideoNotLikeableException;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import com.franklintju.streamlab.videos.VideoStatsRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoLikeService {

    private final VideoLikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final VideoStatsRedisService videoStatsRedisService;
    private final RedisLockService redisLockService;

    private static final String LIKE_LOCK_KEY = "video:like";
    private static final int LOCK_EXPIRE_SECONDS = 10;

    @Transactional
    public void like(Long userId, Long videoId) {
        String lockKey = LIKE_LOCK_KEY + ":" + videoId;
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);

        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            if (likeRepository.existsById(new VideoLikeId(userId, videoId))) {
                throw new AlreadyLikedException();
            }
            User user = userRepository.findById(userId).orElseThrow();
            Video video = videoRepository.findById(videoId).orElseThrow();

            if (!video.isLikeable()) {
                throw new VideoNotLikeableException("视频无法点赞");
            }

            VideoLike like = new VideoLike();
            like.setId(new VideoLikeId(userId, videoId));
            like.setUser(user);
            like.setVideo(video);
            likeRepository.save(like);
            videoStatsRedisService.incrementLikes(videoId, 1);
            log.info("User {} liked video {}", userId, videoId);
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional
    public void unlike(Long userId, Long videoId) {
        String lockKey = LIKE_LOCK_KEY + ":" + videoId;
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);

        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            if (!likeRepository.existsById(new VideoLikeId(userId, videoId))) {
                throw new NotLikedException();
            }
            likeRepository.deleteById(new VideoLikeId(userId, videoId));
            videoStatsRedisService.incrementLikes(videoId, -1);
            log.info("User {} unliked video {}", userId, videoId);
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    public boolean hasLiked(Long userId, Long videoId) {
        return likeRepository.existsById(new VideoLikeId(userId, videoId));
    }

    public long getLikesCount(Long videoId) {
        return likeRepository.countByIdVideoId(videoId);
    }

    public Page<VideoLike> getUserLikes(Long userId, Pageable pageable) {
        return likeRepository.findByIdUserId(userId, pageable);
    }
}
