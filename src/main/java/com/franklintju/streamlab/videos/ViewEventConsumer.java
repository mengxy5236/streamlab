package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ViewEventConsumer {

    private final VideoRepository videoRepository;
    private final VideoStatsRedisService videoStatsRedisService;
    private final ObjectMapper objectMapper;
    private final Map<Long, AtomicInteger> viewBuffer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    private static final int BATCH_SIZE = 50;
    private static final long FLUSH_INTERVAL_SECONDS = 5;

    public ViewEventConsumer(VideoRepository videoRepository,
                             VideoStatsRedisService videoStatsRedisService,
                             ObjectMapper objectMapper) {
        this.videoRepository = videoRepository;
        this.videoStatsRedisService = videoStatsRedisService;
        this.objectMapper = objectMapper;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(this::flushBuffer, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @KafkaListener(topics = KafkaConfig.VIEW_EVENT_TOPIC,
                   groupId = "view-event-group",
                   containerFactory = "viewEventConsumerFactory")
    public void handleViewEvent(String message) {
        try {
            ViewEventMessage event = objectMapper.readValue(message, ViewEventMessage.class);
            log.debug("收到播放事件: videoId={}, userId={}", event.getVideoId(), event.getUserId());

            AtomicInteger counter = viewBuffer.computeIfAbsent(event.getVideoId(), k -> new AtomicInteger(0));
            counter.incrementAndGet();

            if (counter.get() >= BATCH_SIZE) {
                flushVideo(event.getVideoId());
            }
        } catch (Exception e) {
            log.error("播放事件处理失败: {}", message, e);
        }
    }

    private synchronized void flushBuffer() {
        if (viewBuffer.isEmpty()) {
            return;
        }
        Map<Long, AtomicInteger> snapshot = new ConcurrentHashMap<>(viewBuffer);
        viewBuffer.clear();

        for (Map.Entry<Long, AtomicInteger> entry : snapshot.entrySet()) {
            Long videoId = entry.getKey();
            int count = entry.getValue().get();
            if (count > 0) {
                try {
                    videoRepository.incrementViews(videoId, count);
                    videoStatsRedisService.incrementViews(videoId, count);
                    log.info("批量同步播放量: videoId={}, views={}", videoId, count);
                } catch (Exception e) {
                    log.error("批量同步播放量失败: videoId={}", videoId, e);
                    viewBuffer.computeIfAbsent(videoId, k -> new AtomicInteger(0)).addAndGet(count);
                }
            }
        }
    }

    private void flushVideo(Long videoId) {
        AtomicInteger counter = viewBuffer.remove(videoId);
        if (counter != null && counter.get() > 0) {
            try {
                videoRepository.incrementViews(videoId, counter.get());
                videoStatsRedisService.incrementViews(videoId, counter.get());
                log.info("达标批量同步播放量: videoId={}, views={}", videoId, counter.get());
            } catch (Exception e) {
                log.error("同步播放量失败: videoId={}", videoId, e);
                viewBuffer.computeIfAbsent(videoId, k -> new AtomicInteger(0)).addAndGet(counter.get());
            }
        }
    }
}
