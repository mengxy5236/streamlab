package com.franklintju.streamlab.auth;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.users.UserDto;
import com.franklintju.streamlab.users.UserConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "登录、登出、刷新令牌")
@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtConfig jwtConfig;
    private final UserConverter userConverter;
    private final AuthService authService;

    @Operation(summary = "登录", description = "用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<JwtResponse> login(
        @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        var loginResult = authService.login(request);

        var refreshToken = loginResult.getRefreshToken().toString();
        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ApiResponse.success(new JwtResponse(loginResult.getAccessToken().toString()));
    }

    @Operation(summary = "刷新令牌", description = "使用RefreshToken获取新的AccessToken")
    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        var accessToken = authService.refreshAccessToken(refreshToken);
        return ApiResponse.success(new JwtResponse(accessToken.toString()));
    }

    @Operation(summary = "登出", description = "清除RefreshToken Cookie")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        var cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ApiResponse.success(null);
    }

    @Operation(summary = "获取当前用户", description = "获取登录用户信息")
    @GetMapping("/me")
    public ApiResponse<UserDto> me() {
        var user = authService.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        var userDto = userConverter.toDto(user);
        return ApiResponse.success(userDto);
    }
}
