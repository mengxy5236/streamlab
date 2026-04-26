package com.franklintju.streamlab.upload;

import com.franklintju.streamlab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传", description = "视频与封面上传管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "上传视频", description = "上传视频文件并创建转码任务")
    @PostMapping("/{videoId}")
    public ApiResponse<?> uploadVideo(
            @PathVariable Long videoId,
            @RequestParam("file") MultipartFile file) {
        var result = uploadService.uploadVideo(videoId, file);
        return ApiResponse.success(result);
    }

    @Operation(summary = "上传封面", description = "上传视频封面图片")
    @PostMapping("/{videoId}/cover")
    public ApiResponse<?> uploadCover(
            @PathVariable Long videoId,
            @RequestParam("file") MultipartFile file) {
        var result = uploadService.uploadCover(videoId, file);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询任务", description = "查询上传或转码任务状态")
    @GetMapping("/tasks/{taskId}")
    public ApiResponse<UploadTaskDto> getTaskStatus(@PathVariable Long taskId) {
        var task = uploadService.getTask(taskId);
        if (task == null) {
            return ApiResponse.error(404, "任务不存在");
        }
        return ApiResponse.success(task);
    }
}
