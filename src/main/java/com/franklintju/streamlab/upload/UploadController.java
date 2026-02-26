package com.franklintju.streamlab.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/{videoId}")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @PathVariable Long videoId,
            @RequestParam("file") MultipartFile file
    ) {
        var result = uploadService.uploadVideo(videoId, file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<UploadTaskDto> getTaskStatus(@PathVariable Long taskId) {
        var task = uploadService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
