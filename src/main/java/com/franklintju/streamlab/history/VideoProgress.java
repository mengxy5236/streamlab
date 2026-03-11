package com.franklintju.streamlab.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long videoId;
    private Integer progress;
    private Integer duration;
    private LocalDateTime updatedAt;
}
