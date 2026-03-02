package com.franklintju.streamlab.exceptions;

import org.springframework.http.HttpStatus;

public class VideoNotLikeableException extends BusinessException {
    public VideoNotLikeableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
