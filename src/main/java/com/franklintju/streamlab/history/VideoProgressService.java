package com.franklintju.streamlab.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProgressService {

    private static final String KEY_PREFIX = "video:progress:";
    private static final long TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;
    private final WatchHistoryRepository watchHistoryRepository;

    /**
     * 获取播放进度（Redis 优先，失败则返回 null）
     */
    public VideoProgress getProgress(Long userId, Long videoId) {
        String key = buildKey(userId, videoId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof VideoProgress progress) {
            return progress;
        }
        return null;
    }

    /**
     * 保存播放进度到 Redis
     */
    public void saveProgress(Long userId, Long videoId, Integer progress, Integer duration) {
        String key = buildKey(userId, videoId);
        VideoProgress videoProgress = new VideoProgress(userId, videoId, progress, duration, LocalDateTime.now());
        redisTemplate.opsForValue().set(key, videoProgress, TTL_DAYS, TimeUnit.DAYS);
    }

    /**
     * 删除播放进度
     */
    public void deleteProgress(Long userId, Long videoId) {
        String key = buildKey(userId, videoId);
        redisTemplate.delete(key);
    }

    /**
     * 删除用户所有播放进度
     */
    public void deleteUserProgress(Long userId) {
        String pattern = KEY_PREFIX + userId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 定时同步 Redis 中的进度到数据库
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void syncProgressToDatabase() {
        log.info("开始同步播放进度到数据库...");

        var keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int syncedCount = 0;
        for (String key : keys) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (!(value instanceof VideoProgress progress)) {
                    continue;
                }

                var existing = watchHistoryRepository.findByUserIdAndVideoId(progress.getUserId(), progress.getVideoId());
                if (existing.isPresent()) {
                    WatchHistory history = existing.get();
                    history.setProgress(progress.getProgress());
                    history.setDuration(progress.getDuration());
                    watchHistoryRepository.save(history);
                    syncedCount++;
                }
            } catch (Exception e) {
                log.warn("同步进度失败, key={}: {}", key, e.getMessage());
            }
        }

        log.info("播放进度同步完成, 共同步 {} 条记录", syncedCount);
    }

    private String buildKey(Long userId, Long videoId) {
        return KEY_PREFIX + userId + ":" + videoId;
    }
}
