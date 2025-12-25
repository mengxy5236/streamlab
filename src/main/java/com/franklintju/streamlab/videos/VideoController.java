package com.franklintju.streamlab.videos;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/{id}")
    public ResponseEntity<VideoDto> uploadVideo(
            @PathVariable(name = "id") Long userId,
            @RequestBody VideoDto videoDto,
            UriComponentsBuilder uriBuilder
    ){
        var created = videoService.uploadVideo(userId,videoDto);

        var uri = uriBuilder
                .path("/videos/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<VideoDto>> getVideosByUserId(
            @PathVariable(name = "id") Long userId
    ){
        return ResponseEntity.ok(videoService.getVideos(userId));
    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleVideoNotFoundException(VideoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "video not found"));
    }

}
