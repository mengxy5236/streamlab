package com.franklintju.streamlab.users;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("the user is not exists~");
    }
}
