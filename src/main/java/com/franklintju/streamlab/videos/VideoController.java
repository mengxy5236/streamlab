package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "视频", description = "视频 CRUD、播放")
@AllArgsConstructor
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;

    @Operation(summary = "创建视频", description = "创建视频基本信息")
    @PostMapping
    public ApiResponse<VideoDto> createVideo(@RequestBody CreateVideoRequest request) {
        return ApiResponse.success(videoService.createVideo(request));
    }

    @Operation(summary = "更新视频", description = "更新视频信息")
    @PutMapping("/{id}")
    public ApiResponse<VideoDto> updateVideo(@PathVariable Long id, @RequestBody UpdateVideoRequest request) {
        return ApiResponse.success(videoService.updateVideo(id, request));
    }

    @Operation(summary = "删除视频", description = "删除指定视频")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取视频", description = "根据ID获取视频详情")
    @GetMapping("/{id}")
    public ApiResponse<VideoDto> getVideo(@PathVariable Long id) {
        return ApiResponse.success(videoService.getVideo(id));
    }

    @Operation(summary = "获取用户视频", description = "获取指定用户的所有视频")
    @GetMapping
    public ApiResponse<List<VideoDto>> getVideosByUser(@RequestParam("userId") Long userId) {
        return ApiResponse.success(videoService.getVideosByUser(userId));
    }

    @Operation(summary = "增加播放量", description = "增加视频播放次数")
    @PostMapping("/{id}/view")
    public ApiResponse<Void> viewVideo(@PathVariable Long id) {
        videoService.incrementViewCount(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "视频列表", description = "分页获取视频列表")
    @GetMapping("/list")
    public ApiResponse<PageResponse<VideoDto>> listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VideoDto> videos = videoService.listVideos(page, size);
        return ApiResponse.success(PageResponse.of(videos));
    }

    @Operation(summary = "发布视频", description = "发布视频使其可见")
    @PostMapping("/{id}/publish")
    public ApiResponse<VideoDto> publishVideo(@PathVariable Long id) {
        return ApiResponse.success(videoService.publishVideo(id));
    }
}
