package com.franklintju.streamlab.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeProgressMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long videoId;
    private Integer progress;
    private String status;
    private String message;
    private String hlsUrl;
    private Integer duration;
}
