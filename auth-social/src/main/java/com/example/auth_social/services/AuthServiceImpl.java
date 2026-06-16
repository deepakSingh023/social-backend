package com.example.auth_social.services;

import com.example.auth_social.dto.*;
import com.example.auth_social.entity.User;
import com.example.auth_social.exception.EmailAlreadyInUse;
import com.example.auth_social.exception.InvalidCredentials;
import com.example.auth_social.exception.UserNotFound;
import com.example.auth_social.exception.UsernameAlreadyInUse;
import com.example.auth_social.repository.UserRepository;
import com.example.auth_social.tasks.FeignClient;
import com.example.auth_social.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AsyncService asyncService;


    @Value("${service.secret}")
    private String secret;


    @Override
    public UserResponse signup(SignUpRequest request) {
        // check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyInUse("Email already registered");
        }

        // check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyInUse("Username already taken");
        }

        // create new user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .provider("LOCAL")
                .build();

        // save user in DB
        User savedUser = userRepository.save(user);

        CreateProfile data = new CreateProfile(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getId().toString()
        );

        asyncService.createProfiles(data,secret);

        // return UserResponse
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFound(
                        "User not found"));

        // check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentials("Invalid credentials");
        }

        UUID userId = user.getId();

        String token = jwtUtil.generateToken(userId, user.getRole(), user.getEmail());





        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

