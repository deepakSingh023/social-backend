package com.example.auth_social.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID  userId;
    private String token;
    private String  username;
    private String email;
    private String role;
}
