package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.config.KafkaConfig;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscodeConsumer {

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;
    private final HlsService hlsService;
    private final TranscodeProgressService transcodeProgressService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.VIDEO_TRANSCODE_TOPIC, groupId = "streamlab-group")
    @Transactional
    public void handleTranscodeMessage(String message) {
        try {
            TranscodeMessage transcodeMessage = objectMapper.readValue(message, TranscodeMessage.class);
            log.info("收到转码消息: uploadTaskId={}, videoId={}", 
                    transcodeMessage.getUploadTaskId(), transcodeMessage.getVideoId());

            processVideo(transcodeMessage);

        } catch (Exception e) {
            log.error("转码消息处理失败: {}", message, e);
        }
    }

    private void processVideo(TranscodeMessage transcodeMessage) {
        var task = uploadTaskRepository.findById(transcodeMessage.getUploadTaskId()).orElse(null);
        if (task == null) {
            log.error("UploadTask not found: {}", transcodeMessage.getUploadTaskId());
            return;
        }

        String ossUrl = transcodeMessage.getOssUrl();
        Long videoId = transcodeMessage.getVideoId();

        try {
            // 状态：处理中
            task.setStatus(UploadTask.TaskStatus.PROCESSING);
            task.setProgress(5);
            uploadTaskRepository.save(task);
            
            // 发送进度：开始处理
            transcodeProgressService.sendProgress(videoId, 5, "PROCESSING", "开始处理视频");

            HlsService.HlsResult hlsResult = null;

            if (hlsService.isHlsSupported()) {
                try {
                    log.info("开始 HLS 转码: videoId={}, ossUrl={}", videoId, ossUrl);

                    task.setProgress(10);
                    uploadTaskRepository.save(task);
                    
                    // 发送进度：开始转码
                    transcodeProgressService.sendProgress(videoId, 10, "PROCESSING", "开始 HLS 转码");

                    hlsResult = hlsService.convertToHls(ossUrl, videoId);

                    log.info("HLS 转码完成: hlsUrl={}, duration={}s", 
                            hlsResult.hlsUrl(), hlsResult.duration());
                } catch (Exception e) {
                    log.error("HLS 转码失败: {}", e.getMessage());
                    // 发送进度：转码失败
                    transcodeProgressService.sendProgress(videoId, task.getProgress(), "FAILED", "HLS 转码失败: " + e.getMessage());
                }
            } else {
                log.warn("FFmpeg 不可用，跳过 HLS 转码");
                transcodeProgressService.sendProgress(videoId, task.getProgress(), "PROCESSING", "FFmpeg 不可用，跳过 HLS 转码");
            }

            var video = videoRepository.findById(videoId).orElse(null);
            if (video != null) {
                video.setVideoUrl(ossUrl);

                if (hlsResult != null) {
                    video.setHlsUrl(hlsResult.hlsUrl());
                    video.setHlsReady(true);
                    video.setResolution(hlsResult.resolution());
                    video.setBitrate(hlsResult.bitrate());
                    video.setDuration(hlsResult.duration());
                    log.info("视频 {} 时长设置为: {} 秒", video.getId(), hlsResult.duration());
                }

                video.setStatus(Video.VideoStatus.READY);
                videoRepository.save(video);
            }

            // 状态：成功
            task.setStatus(UploadTask.TaskStatus.SUCCESS);
            task.setProgress(100);
            
            // 发送进度：完成
            transcodeProgressService.sendProgress(
                    TranscodeProgressMessage.builder()
                            .taskId(task.getId())
                            .videoId(videoId)
                            .progress(100)
                            .status("SUCCESS")
                            .message("转码完成")
                            .hlsUrl(hlsResult != null ? hlsResult.hlsUrl() : null)
                            .duration(hlsResult != null ? hlsResult.duration() : null)
                            .build()
            );

        } catch (Exception e) {
            log.error("Video processing failed: uploadTaskId={}", transcodeMessage.getUploadTaskId(), e);
            task.setStatus(UploadTask.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            
            // 发送进度：失败
            transcodeProgressService.sendProgress(videoId, task.getProgress(), "FAILED", "处理失败: " + e.getMessage());
        }

        uploadTaskRepository.save(task);
    }
}
