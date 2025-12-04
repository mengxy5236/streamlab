package com.franklintju.streamlab.videos;

import lombok.Data;
import java.time.Instant;

@Data
public class VideoDto {
    Long id;
    String title;
    String description;
    String coverUrl;
    String videoUrl;
    String category;
    Integer viewsCount;
    Integer likesCount;
    Integer coinsCount;
    Integer favoritesCount;
    Integer commentsCount;
    Integer sharesCount;
    Instant updatedAt;
}
