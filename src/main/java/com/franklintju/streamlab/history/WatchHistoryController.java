package com.franklintju.streamlab.history;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "历史记录", description = "观看历史管理")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService historyService;
    private final WatchHistoryConverter historyConverter;

    @Operation(summary = "记录进度", description = "记录视频播放进度")
    @PostMapping("/video/{videoId}")
    public ApiResponse<Void> recordProgress(
            @PathVariable Long videoId,
            @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId();
        Integer progress = body.get("progress");
        Integer duration = body.get("duration");
        historyService.recordProgress(userId, videoId, progress, duration);
        return ApiResponse.success(null);
    }

    @Operation(summary = "我的历史", description = "分页获取当前用户的观看历史")
    @GetMapping
    public ApiResponse<PageResponse<WatchHistoryDto>> getMyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        var history = historyService.getUserHistory(userId, org.springframework.data.domain.PageRequest.of(page, size));
        var response = PageResponse.of(history, historyConverter::toDto);
        return ApiResponse.success(response);
    }

    @Operation(summary = "删除记录", description = "删除指定视频的观看记录")
    @DeleteMapping("/video/{videoId}")
    public ApiResponse<Void> deleteHistory(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        historyService.deleteHistory(userId, videoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "清空历史", description = "清空当前用户的所有观看历史")
    @DeleteMapping
    public ApiResponse<Void> clearHistory() {
        Long userId = getCurrentUserId();
        historyService.clearHistory(userId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取进度", description = "获取视频播放进度（Redis优先）")
    @GetMapping("/video/{videoId}/progress")
    public ApiResponse<VideoProgress> getProgress(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        VideoProgress progress = historyService.getProgress(userId, videoId);
        return ApiResponse.success(progress);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
