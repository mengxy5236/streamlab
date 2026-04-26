package com.franklintju.streamlab.users;

import com.franklintju.streamlab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "资料", description = "用户资料管理")
@AllArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    @Operation(summary = "更新资料", description = "更新用户个人资料")
    @PostMapping("/{id}")
    public ApiResponse<Void> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request) {
        profileService.update(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取资料", description = "获取用户个人资料")
    @GetMapping("/{id}")
    public ApiResponse<ProfileDto> getProfile(@PathVariable Long id) {
        return ApiResponse.success(profileService.getProfile(id));
    }
}
