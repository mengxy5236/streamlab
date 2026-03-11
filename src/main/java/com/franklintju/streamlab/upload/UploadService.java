package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.config.OssService;
import com.franklintju.streamlab.exceptions.VideoNotFoundException;
import com.franklintju.streamlab.videos.Video;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @Transactional
    public Map<String, Object> uploadVideo(Long videoId, MultipartFile file) {
        var user = authService.getCurrentUser();
        var video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);

        if (!video.getUser().getId().equals(user.getId())) {
            throw new SecurityException("无权上传此视频");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType) || !ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的视频格式，仅支持 MP4/AVI/MOV/MKV/WEBM");
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

            videoProcessor.processVideo(task.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", task.getId());
            result.put("videoId", videoId);
            result.put("status", task.getStatus().name());
            result.put("videoUrl", ossUrl);

            return result;

        } catch (IOException e) {
            log.error("File upload failed: {}", videoId, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public Map<String, Object> uploadCover(Long videoId, MultipartFile file) {
        var user = authService.getCurrentUser();
        var video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);

        if (!video.getUser().getId().equals(user.getId())) {
            throw new SecurityException("无权上传此封面");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType) || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG/PNG/GIF/WEBP");
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
            throw new RuntimeException("封面上传失败: " + e.getMessage());
        }
    }

    public UploadTaskDto getTask(Long taskId) {
        var task = uploadTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        return UploadTaskDto.fromEntity(task);
    }
}
