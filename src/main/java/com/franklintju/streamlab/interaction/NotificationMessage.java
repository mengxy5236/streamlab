package com.franklintju.streamlab.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long receiverId;
    private NotificationType type;
    private Long relatedId;
    private Long senderId;
    private String content;
    private Instant timestamp;

    public enum NotificationType {
        LIKE,
        COMMENT,
        FOLLOW,
        REPLY
    }

    public static NotificationMessage of(Long receiverId, NotificationMessage.NotificationType type,
                                          Long relatedId, Long senderId, String content) {
        return NotificationMessage.builder()
                .receiverId(receiverId)
                .type(type)
                .relatedId(relatedId)
                .senderId(senderId)
                .content(content)
                .timestamp(Instant.now())
                .build();
    }
}
