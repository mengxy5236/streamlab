package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessor {

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;

    @Async("videoTaskExecutor")
    @Transactional
    public void processVideo(Long uploadTaskId) {
        var task = uploadTaskRepository.findById(uploadTaskId).orElse(null);
        if (task == null) {
            log.error("UploadTask not found: {}", uploadTaskId);
            return;
        }

        try {
            task.setStatus(UploadTask.TaskStatus.PROCESSING);
            task.setProgress(10);
            uploadTaskRepository.save(task);

            Path videoPath = Paths.get(task.getFilePath());
            if (Files.exists(videoPath)) {
                long fileSize = Files.size(videoPath);
                task.setFileSize(fileSize);
                task.setProgress(30);
                uploadTaskRepository.save(task);

                // TODO: 调用 FFmpeg 提取视频时长
                // Duration duration = extractDuration(videoPath);
                // task.setProgress(80);

                // 模拟处理
                simulateProcessing(task);

                var video = videoRepository.findById(task.getVideoId()).orElse(null);
                if (video != null) {
                    video.setVideoUrl(task.getFilePath());
                    video.setStatus(Video.VideoStatus.READY);
                    videoRepository.save(video);
                }

                task.setStatus(UploadTask.TaskStatus.SUCCESS);
                task.setProgress(100);
                task.setCompletedAt(Instant.now());
            } else {
                task.setStatus(UploadTask.TaskStatus.FAILED);
                task.setErrorMessage("文件不存在: " + task.getFilePath());
            }

        } catch (Exception e) {
            log.error("Video processing failed: {}", uploadTaskId, e);
            task.setStatus(UploadTask.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        }

        uploadTaskRepository.save(task);
    }

    private void simulateProcessing(UploadTask task) throws InterruptedException {
        for (int i = 30; i <= 90; i += 10) {
            Thread.sleep(500);
            task.setProgress(i);
            uploadTaskRepository.save(task);
        }
    }
}
