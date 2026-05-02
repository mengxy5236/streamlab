package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.exceptions.VideoNotFoundException;
import com.franklintju.streamlab.oss.OssService;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/avi", "video/quicktime", "video/x-matroska", "video/webm"
    );
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".avi", ".mov", ".mkv", ".webm"
    );
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;
    private final VideoProcessor videoProcessor;
    private final OssService ossService;
    private final AuthService authService;
    private final ObjectProvider<TranscodeProducer> transcodeProducerProvider;

    @Value("${streamlab.transcode.messaging-enabled:false}")
    private boolean messagingEnabled;

    @Transactional
    public Map<String, Object> uploadVideo(Long videoId, MultipartFile file) {
        var user = authService.getCurrentUser();
        var video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);

        if (!video.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No permission to upload to this video");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType) || !ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported video format, only MP4/AVI/MOV/MKV/WEBM are allowed");
        }

        try {
            String ossUrl = ossService.uploadVideo(file);

            UploadTask task = new UploadTask();
            task.setVideoId(videoId);
            task.setUserId(user.getId());
            task.setFilePath(ossUrl);
            task.setStatus(UploadTask.TaskStatus.CREATED);
            task.setProgress(0);
            uploadTaskRepository.save(task);

            if (messagingEnabled) {
                TranscodeMessage transcodeMessage = new TranscodeMessage(
                        task.getId(),
                        videoId,
                        ossUrl,
                        user.getId()
                );
                var producer = transcodeProducerProvider.getIfAvailable();
                if (producer == null) {
                    throw new IllegalStateException("Transcode messaging is enabled but no producer is available");
                }
                producer.sendTranscodeMessage(transcodeMessage);
            } else {
                videoProcessor.processVideo(task.getId());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", task.getId());
            result.put("videoId", videoId);
            result.put("status", task.getStatus().name());
            result.put("videoUrl", ossUrl);
            result.put("mode", messagingEnabled ? "rabbitmq" : "local-async");

            return result;
        } catch (IOException e) {
            log.error("File upload failed: {}", videoId, e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadCover(Long videoId, MultipartFile file) {
        var user = authService.getCurrentUser();
        var video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);

        if (!video.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No permission to upload the cover for this video");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType) || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image format, only JPG/PNG/GIF/WEBP are allowed");
        }

        try {
            String ossUrl = ossService.uploadCover(file);
            video.setCoverUrl(ossUrl);
            videoRepository.save(video);

            Map<String, Object> result = new HashMap<>();
            result.put("coverUrl", ossUrl);
            return result;
        } catch (IOException e) {
            log.error("Cover upload failed: {}", videoId, e);
            throw new RuntimeException("Cover upload failed: " + e.getMessage());
        }
    }

    public UploadTaskDto getTask(Long taskId) {
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Please sign in first");
        }

        var task = uploadTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        if (!user.getId().equals(task.getUserId())) {
            throw new AccessDeniedException("No permission to view this upload task");
        }
        return UploadTaskDto.fromEntity(task);
    }
}
