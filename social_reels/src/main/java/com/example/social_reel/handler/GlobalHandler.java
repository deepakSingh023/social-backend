package com.example.social_reel.handler;


import com.example.social_reel.dto.ErrorResponse;
import com.example.social_reel.exceptions.ReelNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandler {


    @ExceptionHandler(ReelNotFound.class)
    public ResponseEntity<ErrorResponse> handleReelNotFound(ReelNotFound err){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(err.getMessage()));
    }
}
