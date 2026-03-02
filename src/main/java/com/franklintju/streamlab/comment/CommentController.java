package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.Profile;
import com.franklintju.streamlab.users.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ProfileRepository profileRepository;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        Long videoId = ((Number) request.get("videoId")).longValue();
        String content = (String) request.get("content");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        Long rootId = request.get("rootId") != null ? ((Number) request.get("rootId")).longValue() : null;

        Comment comment = commentService.createComment(userId, videoId, content, parentId, rootId);
        return ResponseEntity.ok(toDto(comment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Comment comment = commentService.updateComment(id, request.get("content"));
        return ResponseEntity.ok(toDto(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<Page<CommentDto>> getVideoComments(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> comments = commentService.getVideoComments(videoId, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(comments.map(this::toDto));
    }

    @GetMapping("/{rootId}/replies")
    public ResponseEntity<Page<CommentDto>> getReplies(
            @PathVariable Long rootId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> comments = commentService.getReplies(rootId, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(comments.map(this::toDto));
    }

    @GetMapping("/video/{videoId}/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long videoId) {
        return ResponseEntity.ok(Map.of("count", commentService.getCommentCount(videoId)));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return ResponseEntity.noContent().build();
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setVideoId(comment.getVideo().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setContent(comment.getContent());
        dto.setLikesCount(comment.getLikesCount());
        dto.setReplyCount(comment.getReplyCount());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }
        if (comment.getRoot() != null) {
            dto.setRootId(comment.getRoot().getId());
        }

        Profile profile = profileRepository.findByUserId(comment.getUser().getId()).orElse(null);
        if (profile != null) {
            dto.setUsername(profile.getUsername());
            dto.setAvatarUrl(profile.getAvatarUrl());
        }

        return dto;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
