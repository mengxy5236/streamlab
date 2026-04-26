package com.franklintju.streamlab.danmaku;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 弹幕 WebSocket 消息 DTO
 *
 * 用于前端和后端之间的弹幕传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DanmakuMessage {

    /** 弹幕 ID */
    private Long id;

    /** 视频 ID */
    private Long videoId;

    /** 发送者用户名（展示用） */
    private Long senderUserId;

    /** 弹幕内容 */
    private String content;

    /** 弹幕出现时间（秒） */
    private BigDecimal sendTime;

    /** 弹幕模式：1-滚动 2-顶部 3-底部 */
    private Byte mode;

    /** 字体大小 */
    private Integer fontSize;

    /** 颜色（RGB 十进制） */
    private Integer color;

    /** 发送时间（用于排序） */
    private Instant createdAt;

    /**
     * 从 Danmaku 实体转换为 WebSocket 消息
     */
    public static DanmakuMessage fromEntity(Danmaku danmaku) {
        return DanmakuMessage.builder()
                .id(danmaku.getId())
                .videoId(danmaku.getVideo() != null ? danmaku.getVideo().getId() : null)
                .senderUserId(danmaku.getUser() != null ? danmaku.getUser().getId() : 0)
                .content(danmaku.getContent())
                .sendTime(danmaku.getSendTime())
                .mode(danmaku.getMode())
                .fontSize(danmaku.getFontSize())
                .color(danmaku.getColor())
                .createdAt(danmaku.getCreatedAt())
                .build();
    }
}
