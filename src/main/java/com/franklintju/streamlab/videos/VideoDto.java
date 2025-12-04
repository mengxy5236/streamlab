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
    private Integer duration;
    private Long fileSize;
    private String status;
    private Instant updatedAt;
    private Instant publishedAt;
}
