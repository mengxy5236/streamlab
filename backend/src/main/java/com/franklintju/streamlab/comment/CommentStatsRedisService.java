package com.franklintju.streamlab.comment;

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
public class CommentStatsRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COMMENT_LIKES_KEY = "comment:likes:";
    private static final Duration STATS_TTL = Duration.ofDays(30);

    public void incrementLikes(Long commentId, long delta) {
        String key = COMMENT_LIKES_KEY + commentId;
        redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, STATS_TTL.toDays(), TimeUnit.DAYS);
    }

    public Long getLikes(Long commentId) {
        String key = COMMENT_LIKES_KEY + commentId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    public void clearStats(Long commentId) {
        redisTemplate.delete(COMMENT_LIKES_KEY + commentId);
    }

    public Set<String> getAllCommentLikeKeys() {
        return redisTemplate.keys(COMMENT_LIKES_KEY + "*");
    }
}
