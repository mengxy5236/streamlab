package com.franklintju.streamlab.history;

import com.franklintju.streamlab.config.RedisConfig;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoProgressService progressService;

    /**
     * 记录播放进度 - Redis 优先，异步同步到数据库
     */
    @Transactional
    public WatchHistory recordProgress(Long userId, Long videoId, Integer progress, Integer duration) {
        // 1. 先保存到 Redis（快速响应）
        progressService.saveProgress(userId, videoId, progress, duration);

        // 2. 同时保存到数据库（持久化）
        Optional<WatchHistory> existing = historyRepository.findByUserIdAndVideoId(userId, videoId);

        User user = userRepository.findById(userId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        WatchHistory history;
        if (existing.isPresent()) {
            history = existing.get();
        } else {
            history = new WatchHistory();
            history.setUser(user);
            history.setVideo(video);
        }

        history.setProgress(progress);
        history.setDuration(duration);
        return historyRepository.save(history);
    }

    /**
     * 获取播放进度 - Redis 优先
     */
    public VideoProgress getProgress(Long userId, Long videoId) {
        // 1. 先从 Redis 获取
        VideoProgress redisProgress = progressService.getProgress(userId, videoId);
        if (redisProgress != null) {
            return redisProgress;
        }

        // 2. Redis 没有则从数据库获取
        Optional<WatchHistory> history = historyRepository.findByUserIdAndVideoId(userId, videoId);
        if (history.isPresent()) {
            WatchHistory h = history.get();
            VideoProgress progress = new VideoProgress(
                    userId, videoId, h.getProgress(), h.getDuration(), 
                    h.getWatchedAt() != null ? h.getWatchedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null
            );
            // 回填 Redis
            progressService.saveProgress(userId, videoId, progress.getProgress(), progress.getDuration());
            return progress;
        }

        return null;
    }

    @Cacheable(value = RedisConfig.CACHE_VIDEO_HISTORY, key = "#userId + '_' + #pageable.pageNumber")
    public Page<WatchHistory> getUserHistory(Long userId, Pageable pageable) {
        return historyRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_VIDEO_HISTORY, key = "#userId + '_*'")
    public void deleteHistory(Long userId, Long videoId) {
        historyRepository.deleteByUserIdAndVideoId(userId, videoId);
        progressService.deleteProgress(userId, videoId);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_VIDEO_HISTORY, key = "#userId + '_*'")
    public void clearHistory(Long userId) {
        historyRepository.deleteByUserId(userId);
        progressService.deleteUserProgress(userId);
    }
}
