package com.franklintju.streamlab.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadTaskDto {

    private Long id;
    private Long videoId;
    private Long userId;
    private String filePath;
    private Long fileSize;
    private String status;
    private Integer progress;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public static UploadTaskDto fromEntity(UploadTask task) {
        UploadTaskDto dto = new UploadTaskDto();
        dto.setId(task.getId());
        dto.setVideoId(task.getVideoId());
        dto.setUserId(task.getUserId());
        dto.setFilePath(task.getFilePath());
        dto.setFileSize(task.getFileSize());
        dto.setStatus(task.getStatus().name());
        dto.setProgress(task.getProgress());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        return dto;
    }
}
