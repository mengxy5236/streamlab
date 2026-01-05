package com.franklintju.streamlab.videos;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;

    @PutMapping("/{id}")
    public ResponseEntity<VideoDto> updateVideo(
            @PathVariable(name = "id") Long id,
            @RequestBody updateVideoRequest request
    ){
        return ResponseEntity.ok(videoService.updateVideo(id,request));
    }


}
