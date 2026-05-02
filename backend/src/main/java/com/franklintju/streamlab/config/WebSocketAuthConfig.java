package com.franklintju.streamlab.config;

import com.franklintju.streamlab.auth.Jwt;
import com.franklintju.streamlab.auth.JwtService;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;
import java.util.List;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new AuthChannelInterceptor(jwtService, userRepository));
    }

    @Slf4j
    private static class AuthChannelInterceptor implements ChannelInterceptor {

        private final JwtService jwtService;
        private final UserRepository userRepository;

        public AuthChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
            this.jwtService = jwtService;
            this.userRepository = userRepository;
        }

        @Override
        public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                List<String> authorization = accessor.getNativeHeader("Authorization");

                String token = null;
                if (authorization != null && !authorization.isEmpty()) {
                    String authHeader = authorization.get(0);
                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }

                if (StringUtils.hasText(token)) {
                    try {
                        Jwt jwt = jwtService.parseToken(token);

                        if (jwt != null && !jwt.isExpired()) {
                            Long userId = jwt.getUserId();
                            
                            User user = userRepository.findById(userId).orElse(null);
                            
                            if (user != null) {
                                UserDetails userDetails = org.springframework.security.core.userdetails.User
                                        .withUsername(user.getId().toString())
                                        .password("")
                                        .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())))
                                        .build();

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                accessor.setUser(authentication);
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                log.debug("WebSocket user authenticated, userId: {}", userId);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket authentication failed: {}", e.getMessage());
                    }
                }
            }

            return message;
        }
    }
}
