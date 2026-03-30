package com.franklintju.streamlab.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long commentId;
    private Long commentOwnerId;
    private Long videoId;
    private EventType eventType;
    private Instant timestamp;

    public enum EventType {
        LIKE, UNLIKE
    }

    public static CommentLikeEventMessage like(Long userId, Long commentId, Long commentOwnerId, Long videoId) {
        return new CommentLikeEventMessage(userId, commentId, commentOwnerId, videoId, EventType.LIKE, Instant.now());
    }

    public static CommentLikeEventMessage unlike(Long userId, Long commentId, Long commentOwnerId, Long videoId) {
        return new CommentLikeEventMessage(userId, commentId, commentOwnerId, videoId, EventType.UNLIKE, Instant.now());
    }
}
