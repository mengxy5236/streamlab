package com.franklintju.streamlab.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * WebSocket 认证配置
 *
 * WebSocket 连接时的 JWT Token 验证：
 * - CONNECT 时从 Header 获取 Token
 * - 验证 Token，设置用户到 SecurityContext
 *
 * 前端连接时需要携带 Token：
 * stompClient.connect({
 *     headers: {
 *         'Authorization': 'Bearer ' + token
 *     }
 * }, callback);
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService;

    /**
     * 配置 WebSocket 通道的拦截器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthChannelInterceptor(userDetailsService));
    }

    /**
     * 认证拦截器
     */
    @Slf4j
    private static class AuthChannelInterceptor implements ChannelInterceptor {

        private final UserDetailsService userDetailsService;

        public AuthChannelInterceptor(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                // 从 Header 获取 Authorization
                List<String> authorization = accessor.getNativeHeader("Authorization");

                String token = null;
                if (authorization != null && !authorization.isEmpty()) {
                    String authHeader = authorization.get(0);
                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }

                // 如果有 Token，验证并设置用户
                if (StringUtils.hasText(token)) {
                    try {
                        // 这里需要根据你的 JWT 实现来验证
                        // 简化示例：假设 token 就是用户名
                        String username = extractUsernameFromToken(token);

                        if (username != null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("WebSocket 用户认证成功: {}", username);
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket 认证失败: {}", e.getMessage());
                    }
                }
            }

            return message;
        }

        /**
         * 从 Token 提取用户名
         * 需要根据你的 JWT 实现来写
         */
        private String extractUsernameFromToken(String token) {
            // TODO: 使用你的 JwtService 解析 Token
            // 这里简化处理，直接返回 token 作为用户名（仅演示）
            // 实际项目中应该解析 JWT 获取用户名
            return token.length() > 100 ? "testuser" : token;
        }
    }
}
