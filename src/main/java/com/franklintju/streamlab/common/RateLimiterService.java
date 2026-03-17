package com.franklintju.streamlab.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private static final String RATE_LIMIT_SCRIPT = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current = redis.call('INCR', key)
            if current == 1 then
                redis.call('EXPIRE', key, window)
            end
            return current
            """;

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class),
                Collections.singletonList(redisKey),
                String.valueOf(maxRequests),
                String.valueOf(windowSeconds)
        );

        if (result == null) {
            log.warn("Rate limit check failed for key: {}", key);
            return true;
        }

        boolean allowed = result <= maxRequests;
        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}, current: {}, max: {}",
                    key, result, maxRequests);
        }

        return allowed;
    }

    public long getCurrentCount(String key, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Object value = redisTemplate.opsForValue().get(redisKey);
        return value != null ? ((Number) value).longValue() : 0;
    }

    public void reset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(redisKey);
        log.info("Rate limit reset for key: {}", key);
    }

    public static class Keys {
        public static String login(String ip) {
            return "login:" + ip;
        }

        public static String like(String userId) {
            return "like:" + userId;
        }

        public static String comment(String userId) {
            return "comment:" + userId;
        }

        public static String upload(String userId) {
            return "upload:" + userId;
        }

        public static String api(String userId, String endpoint) {
            return "api:" + userId + ":" + endpoint;
        }
    }
}
