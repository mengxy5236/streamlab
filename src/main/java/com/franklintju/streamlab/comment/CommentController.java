package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.common.ApiResponse;
import com.franklintju.streamlab.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "评论", description = "视频评论管理")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentConverter commentConverter;

    @Operation(summary = "发表评论", description = "在视频下发表评论，可嵌套回复")
    @PostMapping
    public ApiResponse<CommentDto> createComment(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        Long videoId = ((Number) request.get("videoId")).longValue();
        String content = (String) request.get("content");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        Long rootId = request.get("rootId") != null ? ((Number) request.get("rootId")).longValue() : null;

        Comment comment = commentService.createComment(userId, videoId, content, parentId, rootId);
        return ApiResponse.success(commentConverter.toDto(comment));
    }

    @Operation(summary = "更新评论", description = "修改评论内容")
    @PutMapping("/{id}")
    public ApiResponse<CommentDto> updateComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Comment comment = commentService.updateComment(id, request.get("content"));
        return ApiResponse.success(commentConverter.toDto(comment));
    }

    @Operation(summary = "删除评论", description = "删除指定评论")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取视频评论", description = "分页获取视频下的一级评论")
    @GetMapping("/video/{videoId}")
    public ApiResponse<PageResponse<CommentDto>> getVideoComments(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var comments = commentService.getVideoComments(videoId, org.springframework.data.domain.PageRequest.of(page, size));
        var response = PageResponse.of(comments, commentConverter::toDto);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取回复", description = "分页获取评论的回复")
    @GetMapping("/{rootId}/replies")
    public ApiResponse<PageResponse<CommentDto>> getReplies(
            @PathVariable Long rootId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var comments = commentService.getReplies(rootId, org.springframework.data.domain.PageRequest.of(page, size));
        var response = PageResponse.of(comments, commentConverter::toDto);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取评论数", description = "获取视频的评论总数")
    @GetMapping("/video/{videoId}/count")
    public ApiResponse<Long> getCommentCount(@PathVariable Long videoId) {
        return ApiResponse.success(commentService.getCommentCount(videoId));
    }

    @Operation(summary = "点赞评论", description = "为评论点赞")
    @PostMapping("/{id}/like")
    public ApiResponse<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "取消点赞", description = "取消评论点赞")
    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlikeComment(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return ApiResponse.success(null);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
