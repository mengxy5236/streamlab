package com.franklintju.streamlab.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VideoProgressService {

    private static final String KEY_PREFIX = "video:progress:";
    private static final long TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;

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

    private String buildKey(Long userId, Long videoId) {
        return KEY_PREFIX + userId + ":" + videoId;
    }
}
