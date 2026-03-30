package com.franklintju.streamlab.interaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long videoId;
    private Long videoOwnerId;
    private EventType eventType;
    private Instant timestamp;

    public enum EventType {
        LIKE,
        UNLIKE
    }

    public static LikeEventMessage like(Long userId, Long videoId, Long videoOwnerId) {
        return new LikeEventMessage(userId, videoId, videoOwnerId, EventType.LIKE, Instant.now());
    }

    public static LikeEventMessage unlike(Long userId, Long videoId, Long videoOwnerId) {
        return new LikeEventMessage(userId, videoId, videoOwnerId, EventType.UNLIKE, Instant.now());
    }
}
