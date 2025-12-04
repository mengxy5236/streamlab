package com.franklintju.streamlab.users;

import com.franklintju.streamlab.videos.VideoDto;
import com.franklintju.streamlab.videos.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final VideoService videoService;

    @GetMapping("/{id}/videos")
    public ResponseEntity<List<VideoDto>> getVideosByUserId(
            @PathVariable(name = "id") Long userId
    ){
        return ResponseEntity.ok(videoService.getVideos(userId));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFoundException(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
    }
}
