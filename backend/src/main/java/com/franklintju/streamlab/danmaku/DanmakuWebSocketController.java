package com.franklintju.streamlab.danmaku;

import com.franklintju.streamlab.users.Profile;
import com.franklintju.streamlab.users.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 弹幕 WebSocket 控制器
 *
 * 消息映射说明：
 * - @MessageMapping("/danmaku/send") → 客户端发送消息到 /app/danmaku/send
 * - @SendTo("/topic/video/{videoId}") → 广播到 /topic/video/{videoId}
 *
 * 前端使用示例：
 * // 发送弹幕
 * stompClient.send('/app/danmaku/send', {}, JSON.stringify({
 *     videoId: 123,
 *     content: '666',
 *     sendTime: 10.5,
 *     mode: 1,
 *     color: 16777215
 * }));
 *
 * // 订阅弹幕
 * stompClient.subscribe('/topic/video/123', (message) => {
 *     const danmaku = JSON.parse(message.body);
 *     console.log('收到弹幕:', danmaku);
 * });
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DanmakuWebSocketController {

    private final DanmakuService danmakuService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProfileRepository profileRepository;

    /**
     * 发送弹幕（需要认证）
     *
     * 流程：
     * 1. 客户端发送弹幕到 /app/danmaku/send
     * 2. 服务端验证、保存到数据库
     * 3. 广播到 /topic/video/{videoId}
     */
    @MessageMapping("/danmaku/send")
    public void sendDanmaku(@Payload DanmakuMessage message) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "匿名";

        log.info("收到弹幕: 用户={}, 内容={}, 视频={}",
                username, message.getContent(), message.getVideoId());

        try {
            
            Profile profile = profileRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            
            Danmaku savedDanmaku = danmakuService.sendDanmaku(
                    message.getVideoId(),
                    profile.getUser().getId(),
                    message.getContent(),
                    message.getSendTime(),
                    message.getMode(),
                    message.getFontSize(),
                    message.getColor()
            );

            
            DanmakuMessage broadcastMessage = DanmakuMessage.fromEntity(savedDanmaku);

            messagingTemplate.convertAndSend(
                    "/topic/video/" + message.getVideoId(),
                    broadcastMessage
            );

            log.debug("弹幕已广播: id={}", savedDanmaku.getId());

        } catch (Exception e) {
            log.error("发送弹幕失败: {}", e.getMessage());
            // 可以发送错误消息给发送者
        }
    }

    /**
     * 获取历史弹幕（点对点，只发给请求者）
     *
     * 流程：
     * 1. 客户端请求 /app/danmaku/history/{videoId}
     * 2. 服务端查询数据库
     * 3. 只返回给请求者 /queue/danmaku/history
     */
    @MessageMapping("/danmaku/history/{videoId}")
    @SendToUser("/queue/danmaku/history")
    public List<DanmakuMessage> getHistory(@DestinationVariable Long videoId) {
        log.debug("请求历史弹幕: videoId={}", videoId);

        List<Danmaku> danmakus = danmakuService.getDanmakusByVideoId(videoId);

        return danmakus.stream()
                .map(DanmakuMessage::fromEntity)
                .toList();
    }
}
