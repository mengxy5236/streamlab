package com.franklintju.streamlab.common;

import com.franklintju.streamlab.comment.CommentRepository;
import com.franklintju.streamlab.comment.CommentStatsRedisService;
import com.franklintju.streamlab.videos.VideoRepository;
import com.franklintju.streamlab.videos.VideoStatsRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsSyncScheduler {

    private final VideoStatsRedisService videoStatsRedisService;
    private final CommentStatsRedisService commentStatsRedisService;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(":(\\d+)$");
    private static final Pattern COMMENT_ID_PATTERN = Pattern.compile(":(\\d+)$");

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncVideoStatsToDatabase() {
        syncViews();
        syncLikes();
    }

    private void syncViews() {
        Set<String> viewKeys = videoStatsRedisService.getAllVideoViewKeys();
        for (String key : viewKeys) {
            Long videoId = extractVideoId(key);
            if (videoId == null) {
                continue;
            }

            Long views = videoStatsRedisService.getViews(videoId);
            if (views != 0) {
                try {
                    videoRepository.incrementViews(videoId, views.intValue());
                    videoStatsRedisService.clearViewStats(videoId);
                    log.info("同步播放量到数据库: videoId={}, views={}", videoId, views);
                } catch (Exception e) {
                    log.error("同步播放量失败: videoId={}", videoId, e);
                }
            }
        }
    }

    private void syncLikes() {
        Set<String> likeKeys = videoStatsRedisService.getAllVideoLikeKeys();
        for (String key : likeKeys) {
            Long videoId = extractVideoId(key);
            if (videoId == null) {
                continue;
            }

            Long likes = videoStatsRedisService.getLikes(videoId);
            if (likes != 0) {
                try {
                    videoRepository.incrementLikes(videoId, likes.intValue());
                    videoStatsRedisService.clearLikeStats(videoId);
                    log.info("同步点赞数到数据库: videoId={}, likes={}", videoId, likes);
                } catch (Exception e) {
                    log.error("同步点赞数失败: videoId={}", videoId, e);
                }
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncCommentStatsToDatabase() {
        Set<String> likeKeys = commentStatsRedisService.getAllCommentLikeKeys();

        for (String key : likeKeys) {
            Long commentId = extractCommentId(key);
            if (commentId == null) continue;

            Long likes = commentStatsRedisService.getLikes(commentId);
            if (likes != 0) {
                try {
                    commentRepository.updateLikesCount(commentId, likes.intValue());
                    commentStatsRedisService.clearStats(commentId);
                    log.info("同步评论点赞数到数据库: commentId={}, likes={}", commentId, likes);
                } catch (Exception e) {
                    log.error("同步评论点赞数失败: commentId={}", commentId, e);
                }
            }
        }
    }

    private Long extractVideoId(String key) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(key);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private Long extractCommentId(String key) {
        Matcher matcher = COMMENT_ID_PATTERN.matcher(key);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }
}
