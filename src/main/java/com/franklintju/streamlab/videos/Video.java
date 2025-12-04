package com.franklintju.streamlab.videos;

import com.franklintju.streamlab.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "videos")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VideoStatus status;

    @Column(name = "category")
    private String category;

    @Column(name = "views_count")
    private Integer viewsCount;

    @Column(name = "likes_count")
    private Integer likesCount;

    @Column(name = "coins_count")
    private Integer coinsCount;

    @Column(name = "favorites_count")
    private Integer favoritesCount;

    @Column(name = "comments_count")
    private Integer commentsCount;

    @Column(name = "shares_count")
    private Integer sharesCount;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    private enum VideoStatus {
        UPLOADING, TRANSCODING, PUBLISHED, DELETED
    }

    public static Video createNew(User user, VideoDto dto) {
        Video video = new Video();

        video.setUser(user);
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setCategory(dto.getCategory());

        video.setCoinsCount(0);
        video.setLikesCount(0);
        video.setCommentsCount(0);
        video.setFavoritesCount(0);
        video.setSharesCount(0);
        video.setViewsCount(0);

        video.setStatus(VideoStatus.UPLOADING);
        video.setPublishedAt(Instant.now());
        video.setUpdatedAt(Instant.now());

        return video;
    }
}