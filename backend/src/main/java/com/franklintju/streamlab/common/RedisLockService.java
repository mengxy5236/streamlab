package com.franklintju.streamlab.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_SCRIPT =
            "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then return 1 else return 0 end";
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    public String acquireLock(String key, long expireSeconds) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.execute(
                new DefaultRedisScript<>(LOCK_SCRIPT, Boolean.class),
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(expireSeconds)
        );

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock: {}", lockKey);
            return lockValue;
        }
        log.debug("Failed to acquire lock: {}", lockKey);
        return null;
    }

    public boolean releaseLock(String key, String lockValue) {
        String lockKey = LOCK_PREFIX + key;

        Boolean result = redisTemplate.execute(
                new DefaultRedisScript<>(UNLOCK_SCRIPT, Boolean.class),
                Collections.singletonList(lockKey),
                lockValue
        );

        log.debug("Release lock: {}, result: {}", lockKey, result);
        return Boolean.TRUE.equals(result);
    }

    public void executeWithLock(String key, long expireSeconds, Runnable action) {
        String lockValue = acquireLock(key, expireSeconds);
        if (lockValue == null) {
            throw new RuntimeException("获取锁失败: " + key);
        }
        try {
            action.run();
        } finally {
            releaseLock(key, lockValue);
        }
    }

    public <T> T executeWithLock(String key, long expireSeconds, java.util.concurrent.Callable<T> action) {
        String lockValue = acquireLock(key, expireSeconds);
        if (lockValue == null) {
            throw new RuntimeException("获取锁失败: " + key);
        }
        try {
            return action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseLock(key, lockValue);
        }
    }
}
