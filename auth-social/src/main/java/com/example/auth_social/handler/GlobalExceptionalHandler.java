package com.example.auth_social.handler;
import com.example.auth_social.dto.ErrorResponse;
import com.example.auth_social.exception.EmailAlreadyInUse;
import com.example.auth_social.exception.InvalidCredentials;
import com.example.auth_social.exception.UserNotFound;
import com.example.auth_social.exception.UsernameAlreadyInUse;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;





@RestControllerAdvice
public class GlobalExceptionalHandler {


    @ExceptionHandler(EmailAlreadyInUse.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(EmailAlreadyInUse ex){

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("email already in use please try with another email"));

    }

    @ExceptionHandler(UsernameAlreadyInUse.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyInUse(UsernameAlreadyInUse ex){

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("username already in use please try another username"));

    }


    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFound ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("user doesn't exist"));
    }

    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentials ex){

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body( new ErrorResponse("Invalid credentials"));
    }




}
