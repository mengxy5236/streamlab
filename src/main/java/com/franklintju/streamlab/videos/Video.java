package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.category.Category;
import com.franklintju.streamlab.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // max length : 8h (28800)
    @Column(name = "duration")
    private Integer duration;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "views_count", nullable = false)
    private int viewsCount = 0;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VideoStatus status = VideoStatus.DRAFT;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum VideoStatus {
        DRAFT, READY, PUBLIC, DELETED
    }

    public void markReady(String videoUrl, String coverUrl, Integer duration, Long fileSize) {
        if (status != VideoStatus.DRAFT) {
            throw new IllegalStateException("只有 DRAFT 状态才能标记为 READY:(");
        }
        this.videoUrl = videoUrl;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.fileSize = fileSize;
        this.status = VideoStatus.READY;
    }

    public void publish() {
        if (status != VideoStatus.READY) {
            throw new IllegalStateException("视频未准备好，不能发布:(");
        }
        this.status = VideoStatus.PUBLIC;
        this.publishedAt = Instant.now();
    }

    public void delete() {
        this.status = VideoStatus.DELETED;
    }

    public void view() {
        this.viewsCount++;
    }

    public void like() {
        this.likesCount++;
    }

    public void addComment() {
        this.commentsCount++;
    }

    public void removeComment() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }

}
