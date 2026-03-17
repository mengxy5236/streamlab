package com.franklintju.streamlab.users;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.videos.VideoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Tag(name = "用户", description = "用户管理")
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;

    @Operation(summary = "注册用户", description = "创建新用户账号")
    @PostMapping
    public ApiResponse<UserDto> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {
        var userDto = userService.registerUser(request);
        return ApiResponse.success(userDto);
    }

    @Operation(summary = "获取用户", description = "根据ID获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @Operation(summary = "用户列表", description = "获取所有用户")
    @GetMapping
    public ApiResponse<Iterable<UserDto>> getAllUsers(
            @RequestParam(required = false, defaultValue = "", name = "sort") String sort) {
        return ApiResponse.success(userService.getAllUsers(sort));
    }

    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "修改密码", description = "修改用户密码")
    @PostMapping("/{id}/change-password")
    public ApiResponse<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取用户视频", description = "获取用户发布的所有视频")
    @GetMapping("/{id}/videos")
    public ApiResponse<List<VideoDto>> getVideos(@PathVariable Long id) {
        return ApiResponse.success(userService.getVideos(id));
    }
}
