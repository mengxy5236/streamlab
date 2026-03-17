package com.franklintju.streamlab.videos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoStatsRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VIDEO_VIEWS_KEY = "video:views:";
    private static final String VIDEO_LIKES_KEY = "video:likes:";
    private static final String VIDEO_DANMAKU_KEY = "video:danmaku:";
    private static final Duration STATS_TTL = Duration.ofDays(30);

    public void incrementViews(Long videoId, long delta) {
        String key = VIDEO_VIEWS_KEY + videoId;
        redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, STATS_TTL.toDays(), TimeUnit.DAYS);
    }

    public void incrementLikes(Long videoId, long delta) {
        String key = VIDEO_LIKES_KEY + videoId;
        redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, STATS_TTL.toDays(), TimeUnit.DAYS);
    }

    public void incrementDanmaku(Long videoId, long delta) {
        String key = VIDEO_DANMAKU_KEY + videoId;
        redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, STATS_TTL.toDays(), TimeUnit.DAYS);
    }

    public Long getViews(Long videoId) {
        String key = VIDEO_VIEWS_KEY + videoId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    public Long getLikes(Long videoId) {
        String key = VIDEO_LIKES_KEY + videoId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    public Long getDanmaku(Long videoId) {
        String key = VIDEO_DANMAKU_KEY + videoId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    public void clearStats(Long videoId) {
        redisTemplate.delete(VIDEO_VIEWS_KEY + videoId);
        redisTemplate.delete(VIDEO_LIKES_KEY + videoId);
        redisTemplate.delete(VIDEO_DANMAKU_KEY + videoId);
    }

    public Set<String> getAllVideoViewKeys() {
        return redisTemplate.keys(VIDEO_VIEWS_KEY + "*");
    }

    public Set<String> getAllVideoLikeKeys() {
        return redisTemplate.keys(VIDEO_LIKES_KEY + "*");
    }

    public Set<String> getAllVideoDanmakuKeys() {
        return redisTemplate.keys(VIDEO_DANMAKU_KEY + "*");
    }
}
