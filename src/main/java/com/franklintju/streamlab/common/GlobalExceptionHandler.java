package com.franklintju.streamlab.common;

import com.franklintju.streamlab.follow.AlreadyFollowedException;
import com.franklintju.streamlab.follow.NotFollowedException;
import com.franklintju.streamlab.users.DuplicateUserException;
import com.franklintju.streamlab.users.UserNotFoundException;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFoundException(UserNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found"));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String,String>> handleDuplicateUserException(DuplicateUserException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("conflict", "Phone or Email is already registered."));
    }

    @ExceptionHandler(AlreadyFollowedException.class)
    public ResponseEntity<Map<String,String>> handleAlreadyFollowedException(AlreadyFollowedException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "You have already followed that!"));
    }

    @ExceptionHandler(NotFollowedException.class)
    public ResponseEntity<Map<String,String>> handleNotFollowedException(NotFollowedException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "You have never followed that!"));
    }
}
