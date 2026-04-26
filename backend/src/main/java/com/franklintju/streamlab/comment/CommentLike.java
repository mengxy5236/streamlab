package com.franklintju.streamlab.comment;

import com.franklintju.streamlab.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "comment_id"}))
public class CommentLike {

    @EmbeddedId
    private CommentLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
