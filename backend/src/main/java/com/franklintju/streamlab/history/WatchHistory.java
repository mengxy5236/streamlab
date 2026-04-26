package com.franklintju.streamlab.history;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.videos.Video;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "watch_histories")
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "progress")
    private Integer progress = 0;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(name = "watched_at")
    private Instant watchedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        watchedAt = Instant.now();
    }
}

