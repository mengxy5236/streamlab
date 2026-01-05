package com.franklintju.streamlab.common;

import com.franklintju.streamlab.follow.AlreadyFollowedException;
import com.franklintju.streamlab.follow.NotFollowedException;
import com.franklintju.streamlab.users.DuplicateUserException;
import com.franklintju.streamlab.users.UserNotFoundException;
import com.franklintju.streamlab.videos.VideoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
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

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleVideoNotFoundException(VideoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "video not found"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadableMessage() {
        return ResponseEntity.badRequest().body(new ErrorDto("Invalid request body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }
}
