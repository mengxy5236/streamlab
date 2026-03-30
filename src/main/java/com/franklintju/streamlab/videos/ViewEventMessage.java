package com.franklintju.streamlab.videos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long videoId;
    private Long userId;
    private Long viewDuration;
    private Instant timestamp;
}
