package com.franklintju.streamlab.interaction;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "点赞", description = "视频点赞管理")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class VideoLikeController {

    private final VideoLikeService likeService;

    @Operation(summary = "点赞视频", description = "为视频点赞")
    @PostMapping("/video/{videoId}")
    public ApiResponse<Void> likeVideo(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        likeService.like(userId, videoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "取消点赞", description = "取消视频点赞")
    @DeleteMapping("/video/{videoId}")
    public ApiResponse<Void> unlikeVideo(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        likeService.unlike(userId, videoId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "点赞状态", description = "获取当前用户对视频的点赞状态")
    @GetMapping("/video/{videoId}/status")
    public ApiResponse<?> getLikeStatus(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        return ApiResponse.success(java.util.Map.of(
                "liked", likeService.hasLiked(userId, videoId),
                "count", likeService.getLikesCount(videoId)
        ));
    }

    @Operation(summary = "点赞数", description = "获取视频的点赞总数")
    @GetMapping("/video/{videoId}/count")
    public ApiResponse<Long> getLikesCount(@PathVariable Long videoId) {
        return ApiResponse.success(likeService.getLikesCount(videoId));
    }

    @Operation(summary = "我的点赞", description = "分页获取当前用户的点赞列表")
    @GetMapping("/me")
    public ApiResponse<PageResponse<VideoLike>> getMyLikes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        var likes = likeService.getUserLikes(userId, org.springframework.data.domain.PageRequest.of(page, size));
        return ApiResponse.success(PageResponse.of(likes));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
