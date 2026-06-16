package com.example.auth_social.controller;
import com.example.auth_social.dto.*;
import com.example.auth_social.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignUpRequest request) {
        UserResponse createdUser = authService.signup(request);


        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // current URL = /api/auth/signup
                .path("/{id}")        // append /{id}
                .buildAndExpand(createdUser.getId()) // replace {id} with actual ID
                .toUri();

        // Return 201 Created with Location header and response body
        return ResponseEntity
                .created(location)
                .body(createdUser);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }
}
