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
    private int retryCount;
    private String errorReason;

    public static final int MAX_RETRY = 3;

    public TranscodeMessage(Long uploadTaskId, Long videoId, String ossUrl, Long userId) {
        this.uploadTaskId = uploadTaskId;
        this.videoId = videoId;
        this.ossUrl = ossUrl;
        this.userId = userId;
        this.retryCount = 0;
    }

    public boolean canRetry() {
        return retryCount < MAX_RETRY;
    }

    public TranscodeMessage withIncrementedRetry() {
        return new TranscodeMessage(uploadTaskId, videoId, ossUrl, userId, retryCount + 1, null);
    }

    public TranscodeMessage withError(String error) {
        return new TranscodeMessage(uploadTaskId, videoId, ossUrl, userId, retryCount, error);
    }
}
