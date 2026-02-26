package com.franklintju.streamlab.videos;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;

    @PostMapping
    public ResponseEntity<VideoDto> createVideo(
            @RequestBody CreateVideoRequest request
    ) {
        return ResponseEntity.ok(videoService.createVideo(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoDto> updateVideo(
            @PathVariable Long id,
            @RequestBody UpdateVideoRequest request
    ) {
        return ResponseEntity.ok(videoService.updateVideo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDto> getVideo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideo(id));
    }

    @GetMapping
    public ResponseEntity<List<VideoDto>> getVideosByUser(
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(videoService.getVideosByUser(userId));
    }



    @PostMapping("/{id}/view")
    public ResponseEntity<Void> viewVideo(@PathVariable Long id) {
        videoService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<Page<VideoDto>> listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(videoService.listVideos(page, size));
    }
}
