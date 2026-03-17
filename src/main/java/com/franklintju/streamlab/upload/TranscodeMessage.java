package com.franklintju.streamlab.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long uploadTaskId;
    private Long videoId;
    private String ossUrl;
    private Long userId;
}
