package com.franklintju.streamlab.history;

import lombok.Data;
import java.time.Instant;

@Data
public class WatchHistoryDto {
    private Long id;
    private Long videoId;
    private String videoTitle;
    private String videoCoverUrl;
    private Integer progress;
    private Integer duration;
    private Instant watchedAt;
}
