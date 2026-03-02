package com.franklintju.streamlab.exceptions;

import org.springframework.http.HttpStatus;

public class NotLikedException extends BusinessException {
    public NotLikedException() {
        super("还未点赞，无法取消", HttpStatus.BAD_REQUEST);
    }
}
