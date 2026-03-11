package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.history.WatchHistory;
import com.franklintju.streamlab.history.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProgressService {

    private static final String PROGRESS_KEY_PREFIX = "video:progress:";
    private static final Duration PROGRESS_TTL = Duration.ofDays(14);

    private final RedisTemplate<String, Object> redisTemplate;
    private final WatchHistoryRepository watchHistoryRepository;

    /**
     * 保存播放进度到 Redis（快速写入）
     */
    public void saveProgress(Long userId, Long videoId, Integer progress, Integer duration) {
        String key = buildKey(userId, videoId);
        redisTemplate.opsForHash().put(key, "progress", progress);
        redisTemplate.opsForHash().put(key, "duration", duration);
        redisTemplate.expire(key, PROGRESS_TTL);
    }

    /**
     * 获取播放进度（Redis 优先）
     */
    public Map<String, Object> getProgress(Long userId, Long videoId) {
        String key = buildKey(userId, videoId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        
        if (entries.isEmpty()) {
            return null;
        }
        
        return Map.of(
                "progress", Integer.parseInt(entries.get("progress").toString()),
                "duration", Integer.parseInt(entries.get("duration").toString())
        );
    }

    /**
     * 定时同步 Redis 中的进度到数据库
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void syncProgressToDatabase() {
        log.info("开始同步播放进度到数据库...");
        
        var keys = redisTemplate.keys(PROGRESS_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int syncedCount = 0;
        for (String key : keys) {
            try {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                if (entries.isEmpty()) continue;

                String[] parts = key.replace(PROGRESS_KEY_PREFIX, "").split(":");
                if (parts.length != 2) continue;

                Long userId = Long.parseLong(parts[0]);
                Long videoId = Long.parseLong(parts[1]);
                Integer progress = Integer.parseInt(entries.get("progress").toString());
                Integer duration = Integer.parseInt(entries.get("duration").toString());

                // 查找或创建 WatchHistory 记录
                var existing = watchHistoryRepository.findByUserIdAndVideoId(userId, videoId);
                if (existing.isPresent()) {
                    WatchHistory history = existing.get();
                    history.setProgress(progress);
                    history.setDuration(duration);
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
        return PROGRESS_KEY_PREFIX + userId + ":" + videoId;
    }
}
