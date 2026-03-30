package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.common.DistributedLock;
import com.franklintju.streamlab.exceptions.AlreadyLikedException;
import com.franklintju.streamlab.exceptions.NotLikedException;
import com.franklintju.streamlab.exceptions.VideoNotLikeableException;
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
    private final VideoStatsRedisService videoStatsRedisService;
    private final LikeEventProducer likeEventProducer;

    @Transactional(readOnly = true)
    public boolean hasLiked(Long userId, Long videoId) {
        return likeRepository.existsById(new VideoLikeId(userId, videoId));
    }

    @Transactional(readOnly = true)
    public long getLikesCount(Long videoId) {
        return likeRepository.countByIdVideoId(videoId);
    }

    @Transactional(readOnly = true)
    public Page<VideoLike> getUserLikes(Long userId, Pageable pageable) {
        return likeRepository.findByIdUserId(userId, pageable);
    }

    @DistributedLock(key = "video:like:#videoId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void like(Long userId, Long videoId) {
        if (likeRepository.existsById(new VideoLikeId(userId, videoId))) {
            throw new AlreadyLikedException();
        }

        Video video = videoRepository.findByIdWithUser(videoId).orElseThrow();
        if (!video.isLikeable()) {
            throw new VideoNotLikeableException("视频无法点赞");
        }

        videoStatsRedisService.incrementLikes(videoId, 1);

        LikeEventMessage event = LikeEventMessage.like(userId, videoId, video.getUser().getId());
        likeEventProducer.sendLikeEvent(event);

        log.info("User {} liked video {} (Redis计数+Kafka消息已发送)", userId, videoId);
    }

    @DistributedLock(key = "video:like:#videoId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void unlike(Long userId, Long videoId) {
        if (!likeRepository.existsById(new VideoLikeId(userId, videoId))) {
            throw new NotLikedException();
        }

        Video video = videoRepository.findByIdWithUser(videoId).orElseThrow();

        videoStatsRedisService.incrementLikes(videoId, -1);

        LikeEventMessage event = LikeEventMessage.unlike(userId, videoId, video.getUser().getId());
        likeEventProducer.sendLikeEvent(event);

        log.info("User {} unliked video {} (Redis计数+Kafka消息已发送)", userId, videoId);
    }
}
