package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessor {

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;
    private final HlsService hlsService;

    @Async("videoTaskExecutor")
    @Transactional
    public void processVideo(Long uploadTaskId) {
        var task = uploadTaskRepository.findById(uploadTaskId).orElse(null);
        if (task == null) {
            log.error("UploadTask not found: {}", uploadTaskId);
            return;
        }

        String ossUrl = task.getFilePath();
        
        try {
            task.setStatus(UploadTask.TaskStatus.PROCESSING);
            task.setProgress(5);
            uploadTaskRepository.save(task);

            HlsService.HlsResult hlsResult = null;
            
            if (hlsService.isHlsSupported()) {
                try {
                    log.info("开始 HLS 转码: videoId={}, ossUrl={}", task.getVideoId(), ossUrl);
                    
                    task.setProgress(10);
                    uploadTaskRepository.save(task);
                    
                    hlsResult = hlsService.convertToHls(ossUrl, task.getVideoId());
                    
                    log.info("HLS 转码完成: hlsUrl={}, duration={}s", hlsResult.hlsUrl(), hlsResult.duration());
                } catch (Exception e) {
                    log.error("HLS 转码失败: {}", e.getMessage());
                }
            } else {
                log.warn("FFmpeg 不可用，跳过 HLS 转码");
            }

            var video = videoRepository.findById(task.getVideoId()).orElse(null);
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

            task.setStatus(UploadTask.TaskStatus.SUCCESS);
            task.setProgress(100);
            task.setCompletedAt(Instant.now());

        } catch (Exception e) {
            log.error("Video processing failed: uploadTaskId={}", uploadTaskId, e);
            task.setStatus(UploadTask.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        }

        uploadTaskRepository.save(task);
    }
}
