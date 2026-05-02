package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.config.RabbitMqConfig;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "streamlab.transcode", name = "messaging-enabled", havingValue = "true")
public class TranscodeConsumer {

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;
    private final HlsService hlsService;
    private final TranscodeProgressService transcodeProgressService;
    private final TranscodeProducer transcodeProducer;

    @RabbitListener(queues = RabbitMqConfig.VIDEO_TRANSCODE_QUEUE)
    @Transactional
    public void handleTranscodeMessage(TranscodeMessage transcodeMessage) {
        log.info("Received transcode message: uploadTaskId={}, videoId={}, retryCount={}",
                transcodeMessage.getUploadTaskId(), transcodeMessage.getVideoId(), transcodeMessage.getRetryCount());
        processVideo(transcodeMessage);
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
            task.setStatus(UploadTask.TaskStatus.PROCESSING);
            task.setProgress(5);
            uploadTaskRepository.save(task);

            transcodeProgressService.sendProgress(videoId, 5, "PROCESSING", "Started processing video");

            HlsService.HlsResult hlsResult = null;

            if (hlsService.isHlsSupported()) {
                try {
                    log.info("Starting HLS transcode: videoId={}, ossUrl={}", videoId, ossUrl);

                    task.setProgress(10);
                    uploadTaskRepository.save(task);
                    transcodeProgressService.sendProgress(videoId, 10, "PROCESSING", "Starting HLS transcode");

                    hlsResult = hlsService.convertToHls(ossUrl, videoId);

                    log.info("HLS transcode completed: hlsUrl={}, duration={}s",
                            hlsResult.hlsUrl(), hlsResult.duration());
                } catch (Exception e) {
                    log.error("HLS transcode failed: {}", e.getMessage());
                    transcodeProgressService.sendProgress(videoId, task.getProgress(), "FAILED",
                            "HLS transcode failed: " + e.getMessage());
                    throw e;
                }
            } else {
                log.warn("FFmpeg is unavailable; skipping HLS transcode");
                transcodeProgressService.sendProgress(videoId, task.getProgress(), "PROCESSING",
                        "FFmpeg is unavailable; skipping HLS transcode");
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
                    log.info("Updated video duration: videoId={}, duration={}s", video.getId(), hlsResult.duration());
                }

                video.setStatus(Video.VideoStatus.READY);
                videoRepository.save(video);
            }

            task.setStatus(UploadTask.TaskStatus.SUCCESS);
            task.setProgress(100);
            transcodeProgressService.sendProgress(
                    TranscodeProgressMessage.builder()
                            .taskId(task.getId())
                            .videoId(videoId)
                            .progress(100)
                            .status("SUCCESS")
                            .message("Transcode completed")
                            .hlsUrl(hlsResult != null ? hlsResult.hlsUrl() : null)
                            .duration(hlsResult != null ? hlsResult.duration() : null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Video processing failed: uploadTaskId={}", transcodeMessage.getUploadTaskId(), e);

            if (transcodeMessage.canRetry()) {
                TranscodeMessage retryMessage = transcodeMessage.withIncrementedRetry();
                log.info("Scheduling transcode retry: uploadTaskId={}, attempt={}/{}",
                        retryMessage.getUploadTaskId(), retryMessage.getRetryCount(), TranscodeMessage.MAX_RETRY);
                transcodeProducer.sendTranscodeMessageWithDelay(retryMessage);
            } else {
                log.error("Transcode retries exhausted: uploadTaskId={}", transcodeMessage.getUploadTaskId());
                task.setStatus(UploadTask.TaskStatus.FAILED);
                task.setErrorMessage("Transcode failed after " + TranscodeMessage.MAX_RETRY + " retries: " + e.getMessage());
                transcodeProgressService.sendProgress(videoId, task.getProgress(), "FAILED",
                        "Transcode failed after " + TranscodeMessage.MAX_RETRY + " retries");
            }
        }

        uploadTaskRepository.save(task);
    }
}
