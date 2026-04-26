package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.videos.Video;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_id")
    private Comment root;

    @Column(name = "content")
    private String content;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CommentStatus status = CommentStatus.VISIBLE;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum CommentStatus {
        VISIBLE, HIDDEN, DELETED
    }
}

