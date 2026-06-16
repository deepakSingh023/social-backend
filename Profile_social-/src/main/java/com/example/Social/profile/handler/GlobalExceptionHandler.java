package com.example.Social.profile.handler;


import com.example.Social.profile.dto.ErrorResponse;
import com.example.Social.profile.exceptions.ProfileNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ProfileNotFound.class)
    public ResponseEntity<ErrorResponse> ProfileErrorHandler(ProfileNotFound ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("profile not found"));

    }
}
