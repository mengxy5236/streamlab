package com.franklintju.streamlab.danmaku;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 弹幕服务层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DanmakuService {

    private final DanmakuRepository danmakuRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    /**
     * 发送弹幕
     *
     * @param videoId   视频 ID
     * @param userId    用户 ID
     * @param content   弹幕内容
     * @param sendTime  弹幕出现时间（秒）
     * @param mode      弹幕模式：1-滚动 2-顶部 3-底部
     * @param fontSize  字体大小
     * @param color     颜色（RGB）
     * @return 保存后的弹幕
     */
    @Transactional
    public Danmaku sendDanmaku(Long videoId, Long userId, String content,
                               BigDecimal sendTime, Byte mode, Integer fontSize, Integer color) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("视频不存在: " + videoId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        Danmaku danmaku = Danmaku.builder()
                .video(video)
                .user(user)
                .content(content)
                .sendTime(sendTime != null ? sendTime : BigDecimal.ZERO)
                .mode(mode != null ? mode : 1)
                .fontSize(fontSize != null ? fontSize : 25)
                .color(color != null ? color : 16777215)
                .build();

        Danmaku saved = danmakuRepository.save(danmaku);
        log.info("弹幕保存成功: id={}, videoId={}, userId={}", saved.getId(), videoId, userId);

        return saved;
    }

    /**
     * 简化版发送弹幕（WebSocket 用，不传 userId，从认证上下文获取）
     */
    @Transactional
    public Danmaku sendDanmaku(Long videoId, String content,
                               BigDecimal sendTime, Byte mode, Integer fontSize, Integer color) {
        // 这个方法需要由调用者传入 User，或者从 SecurityContext 获取
        throw new UnsupportedOperationException("请使用带 userId 的方法");
    }

    /**
     * 获取视频的所有弹幕
     */
    @Transactional(readOnly = true)
    public List<Danmaku> getDanmakusByVideoId(Long videoId) {
        return danmakuRepository.findByVideoIdOrderBySendTime(videoId);
    }

    /**
     * 获取视频指定时间范围的弹幕
     */
    @Transactional(readOnly = true)
    public List<Danmaku> getDanmakusByTimeRange(Long videoId, BigDecimal start, BigDecimal end) {
        return danmakuRepository.findByVideoIdAndTimeRange(videoId, start, end);
    }

    /**
     * 获取弹幕数量
     */
    @Transactional(readOnly = true)
    public long getDanmakuCount(Long videoId) {
        return danmakuRepository.countByVideoId(videoId);
    }
}
