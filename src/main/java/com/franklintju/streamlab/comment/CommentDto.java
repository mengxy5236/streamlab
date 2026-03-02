package com.franklintju.streamlab.comment;

import lombok.Data;
import java.time.Instant;

@Data
public class CommentDto {
    private Long id;
    private Long videoId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String content;
    private Long parentId;
    private Long rootId;
    private Integer likesCount;
    private Integer replyCount;
    private Instant createdAt;
    private Instant updatedAt;
}
