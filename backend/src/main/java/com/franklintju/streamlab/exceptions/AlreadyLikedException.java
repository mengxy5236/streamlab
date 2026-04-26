package com.franklintju.streamlab.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyLikedException extends BusinessException {
    public AlreadyLikedException() {
        super("已经点赞过了", HttpStatus.BAD_REQUEST);
    }
}
