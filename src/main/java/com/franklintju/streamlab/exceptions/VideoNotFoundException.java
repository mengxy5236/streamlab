package com.franklintju.streamlab.exceptions;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException() {
        super("There is no video with that name!");
    }
}
