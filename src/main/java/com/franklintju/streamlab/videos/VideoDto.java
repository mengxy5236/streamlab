package com.franklintju.streamlab.videos;

import lombok.Data;

import java.time.Instant;

@Data
public class VideoDto {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private String hlsUrl;
    private boolean hlsReady;
    private String resolution;
    private Integer bitrate;
    private Integer duration;
    private Instant updatedAt;
    private Instant publishedAt;
}
