package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "关注", description = "用户关注管理")
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class FollowController {
    private final FollowService followService;

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/follow")
    public ApiResponse<Void> follow(@RequestBody FollowRequest request) {
        followService.follow(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @PostMapping("/unfollow")
    public ApiResponse<Void> unfollow(@RequestBody FollowRequest request) {
        followService.unfollow(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取关注列表", description = "获取用户关注的人")
    @GetMapping("/{id}/following")
    public ApiResponse<?> getFollowingList(@PathVariable Long id) {
        return ApiResponse.success(followService.getFollowingList(id));
    }

    @Operation(summary = "获取粉丝列表", description = "获取用户的粉丝")
    @GetMapping("/{id}/follower")
    public ApiResponse<?> getFollowerList(@PathVariable Long id) {
        return ApiResponse.success(followService.getFollowerList(id));
    }
}
