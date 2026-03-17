package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.common.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoHotCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final VideoRepository videoRepository;
    private final RedisLockService redisLockService;

    private static final String HOT_VIDEO_RANK_KEY = "rank:videos:views";
    private static final String HOT_VIDEO_LIST_KEY = "hot:videos:list";
    private static final String VIDEO_SCORE_KEY = "video:score:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final int HOT_VIDEO_COUNT = 50;
    private static final String HOT_LOCK_KEY = "video:hot:refresh";
    private static final int LOCK_EXPIRE_SECONDS = 30;

    public void incrementViews(Long videoId, int delta) {
        String key = VIDEO_SCORE_KEY + videoId;
        redisTemplate.opsForZSet().incrementScore(HOT_VIDEO_RANK_KEY, videoId, delta);
        redisTemplate.opsForValue().set(key, delta, CACHE_TTL);
        log.debug("Video {} views incremented by {}", videoId, delta);
    }

    public void updateVideoScore(Long videoId, double score) {
        redisTemplate.opsForZSet().add(HOT_VIDEO_RANK_KEY, videoId, score);
        log.debug("Video {} score updated to {}", videoId, score);
    }

    public List<Long> getHotVideoIds(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> results = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_VIDEO_RANK_KEY, 0, limit - 1);

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(tuple -> (Long) tuple.getValue())
                .collect(Collectors.toList());
    }

    public List<Long> getAllRankedVideoIds() {
        Set<ZSetOperations.TypedTuple<Object>> results = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_VIDEO_RANK_KEY, 0, -1);

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(tuple -> (Long) tuple.getValue())
                .collect(Collectors.toList());
    }

    public Page<Video> getHotVideos(Pageable pageable) {
        List<Long> hotVideoIds = getHotVideoIds(pageable.getPageSize() * (pageable.getPageNumber() + 1));

        if (hotVideoIds.isEmpty()) {
            return refreshHotVideosFromDb(pageable);
        }

        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), hotVideoIds.size());

        if (start >= hotVideoIds.size()) {
            return refreshHotVideosFromDb(pageable);
        }

        List<Long> pageIds = hotVideoIds.subList(start, end);
        List<Video> videos = videoRepository.findAllById(pageIds);

        videos.sort((v1, v2) -> {
            int idx1 = pageIds.indexOf(v1.getId());
            int idx2 = pageIds.indexOf(v2.getId());
            return Integer.compare(idx1, idx2);
        });

        return new org.springframework.data.domain.PageImpl<>(videos, pageable, hotVideoIds.size());
    }

    public Page<Video> refreshHotVideosFromDb(Pageable pageable) {
        String lockValue = redisLockService.acquireLock(HOT_LOCK_KEY, LOCK_EXPIRE_SECONDS);

        if (lockValue == null) {
            log.debug("Hot videos refresh already in progress, using cached data");
            return getHotVideos(pageable);
        }

        try {
            Pageable sortedPageable = PageRequest.of(0, HOT_VIDEO_COUNT, Sort.by("viewsCount").descending());
            Page<Video> hotVideos = videoRepository.findByStatus(Video.VideoStatus.PUBLIC, sortedPageable);

            for (Video video : hotVideos.getContent()) {
                redisTemplate.opsForZSet().add(HOT_VIDEO_RANK_KEY, video.getId(), video.getViewsCount());
            }

            redisTemplate.expire(HOT_VIDEO_RANK_KEY, CACHE_TTL);
            log.info("Hot videos refreshed from DB, count: {}", hotVideos.getNumberOfElements());

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), hotVideos.getContent().size());

            if (start >= hotVideos.getContent().size()) {
                return Page.empty(pageable);
            }

            return new org.springframework.data.domain.PageImpl<>(
                    hotVideos.getContent().subList(start, end),
                    pageable,
                    hotVideos.getTotalElements()
            );
        } finally {
            redisLockService.releaseLock(HOT_LOCK_KEY, lockValue);
        }
    }

    public void clearHotCache() {
        redisTemplate.delete(HOT_VIDEO_RANK_KEY);
        log.info("Hot video cache cleared");
    }
}
