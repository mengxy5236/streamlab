package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.videos.VideoNotFoundException;
import com.franklintju.streamlab.videos.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    private final UploadTaskRepository uploadTaskRepository;
    private final VideoRepository videoRepository;
    private final VideoProcessor videoProcessor;
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

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            UploadTask task = new UploadTask();
            task.setVideoId(videoId);
            task.setUserId(user.getId());
            task.setFilePath(filePath.toString());
            task.setStatus(UploadTask.TaskStatus.CREATED);
            task.setProgress(0);
            uploadTaskRepository.save(task);

            videoProcessor.processVideo(task.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", task.getId());
            result.put("videoId", videoId);
            result.put("status", task.getStatus().name());
            result.put("filePath", filePath.toString());

            return result;

        } catch (IOException e) {
            log.error("File upload failed: {}", videoId, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public UploadTaskDto getTask(Long taskId) {
        var task = uploadTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        return UploadTaskDto.fromEntity(task);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
    }
}
