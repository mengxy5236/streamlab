package com.franklintju.streamlab.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class VideoLikeController {

    private final VideoLikeService likeService;

    @PostMapping("/video/{videoId}")
    public ResponseEntity<Void> likeVideo(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        likeService.like(userId, videoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/video/{videoId}")
    public ResponseEntity<Void> unlikeVideo(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        likeService.unlike(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/video/{videoId}/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Long videoId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(Map.of(
                "liked", likeService.hasLiked(userId, videoId),
                "count", likeService.getLikesCount(videoId)
        ));
    }

    @GetMapping("/video/{videoId}/count")
    public ResponseEntity<Map<String, Long>> getLikesCount(@PathVariable Long videoId) {
        return ResponseEntity.ok(Map.of("count", likeService.getLikesCount(videoId)));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<VideoLike>> getMyLikes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(likeService.getUserLikes(userId, org.springframework.data.domain.PageRequest.of(page, size)));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
