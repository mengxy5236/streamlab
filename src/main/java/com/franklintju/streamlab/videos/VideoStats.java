package com.franklintju.streamlab.videos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "video_stats")
public class VideoStats {

    @Id
    @Column(name = "video_id")
    private Long videoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "views_count")
    private Long viewsCount = 0L;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "coins_count")
    private Integer coinsCount = 0;

    @Column(name = "favorites_count")
    private Integer favoritesCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "shares_count")
    private Integer sharesCount = 0;

    @Column(name = "danmaku_count")
    private Integer danmakuCount = 0;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public static VideoStats createFor(Video video) {
        VideoStats stats = new VideoStats();
        stats.setVideo(video);
        stats.setVideoId(video.getId());
        return stats;
    }
}

