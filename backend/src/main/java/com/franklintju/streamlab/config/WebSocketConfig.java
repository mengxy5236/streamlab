package com.franklintju.streamlab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 *
 * 核心概念：
 * - STOMP: Simple Text Oriented Messaging Protocol
 *         一种简单的文本消息协议，WebSocket 的"上层协议"
 * - MessageBroker: 消息代理，负责转发消息
 *         我们使用内存代理，简单场景够用了
 * - /topic: 广播模式，订阅者都能收到消息
 *         用于弹幕广播：A发弹幕 → 服务器 → 所有订阅这个视频的人
 * - /queue: 点对点模式，只发给特定人
 *         用于通知、私信等
 */
@Configuration
@EnableWebSocketMessageBroker  // 启用 WebSocket 消息代理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     *
     * Spring 会自动创建两个目的地：
     * - /topic/*   → 广播，比如 /topic/video/123 表示视频123的弹幕频道
     * - /queue/*   → 点对点消息
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // 启用内存消息代理
        config.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀（服务端接收的前缀）
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册 STOMP 端点
     *
     * 端点就是客户端连接服务器的地址
     * - /ws → 客户端连接的地址
     * - withSockJS() → 支持 SockJS 降级（如果浏览器不支持 WebSocket）
     *
     * 完整连接地址：ws://localhost:8080/ws
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                    // 端点地址
                .setAllowedOriginPatterns("*")       // 允许跨域（开发环境，生产环境要限制）
                .withSockJS();                        // 启用 SockJS 降级支持
    }
}
