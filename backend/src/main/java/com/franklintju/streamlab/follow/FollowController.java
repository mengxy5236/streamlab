package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Follow", description = "User follow management")
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class FollowController {
    private final FollowService followService;
    private final AuthService authService;

    @Operation(summary = "Follow user", description = "Follow another user")
    @PostMapping("/follow")
    public ApiResponse<Void> follow(@RequestBody FollowRequest request) {
        followService.follow(currentUserId(), request.getFollowingId());
        return ApiResponse.success(null);
    }

    @Operation(summary = "Unfollow user", description = "Unfollow another user")
    @PostMapping("/unfollow")
    public ApiResponse<Void> unfollow(@RequestBody FollowRequest request) {
        followService.unfollow(currentUserId(), request.getFollowingId());
        return ApiResponse.success(null);
    }

    @Operation(summary = "Following list", description = "Get users followed by this user")
    @GetMapping("/{id}/following")
    public ApiResponse<?> getFollowingList(@PathVariable Long id) {
        return ApiResponse.success(followService.getFollowingList(id));
    }

    @Operation(summary = "Follower list", description = "Get this user's followers")
    @GetMapping("/{id}/follower")
    public ApiResponse<?> getFollowerList(@PathVariable Long id) {
        return ApiResponse.success(followService.getFollowerList(id));
    }

    private Long currentUserId() {
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Please sign in first");
        }
        return user.getId();
    }
}
