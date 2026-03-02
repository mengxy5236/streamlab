package com.franklintju.streamlab.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService historyService;

    @PostMapping("/video/{videoId}")
    public ResponseEntity<Void> recordProgress(
            @PathVariable Long videoId,
            @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId();
        Integer progress = body.get("progress");
        Integer duration = body.get("duration");
        historyService.recordProgress(userId, videoId, progress, duration);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<WatchHistoryDto>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<WatchHistory> history = historyService.getUserHistory(userId, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(history.map(this::toDto));
    }

    @DeleteMapping("/video/{videoId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        historyService.deleteHistory(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearHistory() {
        Long userId = getCurrentUserId();
        historyService.clearHistory(userId);
        return ResponseEntity.noContent().build();
    }

    private WatchHistoryDto toDto(WatchHistory history) {
        WatchHistoryDto dto = new WatchHistoryDto();
        dto.setId(history.getId());
        dto.setVideoId(history.getVideo().getId());
        dto.setVideoTitle(history.getVideo().getTitle());
        dto.setVideoCoverUrl(history.getVideo().getCoverUrl());
        dto.setProgress(history.getProgress());
        dto.setDuration(history.getDuration());
        dto.setWatchedAt(history.getWatchedAt());
        return dto;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
