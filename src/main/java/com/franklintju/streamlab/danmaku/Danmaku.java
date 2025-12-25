package com.franklintju.streamlab.danmaku;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.videos.Video;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "danmaku")
public class Danmaku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, length = 100)
    private String content;

    @Column(name = "send_time", nullable = false, precision = 10, scale = 3)
    private BigDecimal sendTime;

    @Column(name = "mode")
    private Byte mode = 1;

    @Column(name = "font_size")
    private Integer fontSize = 25;

    @Column(name = "color")
    private Integer color = 16777215;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

