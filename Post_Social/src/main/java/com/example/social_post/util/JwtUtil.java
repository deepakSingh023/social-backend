package com.example.social_post.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check expiration manually
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                throw new RuntimeException("Token has expired");
            }

            return claims.getSubject(); // userId stored in subject
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired");
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token");
        }
    }

  
    public boolean isTokenValid(String token) {
        try {
            extractUserId(token); // will throw if invalid/expired
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
