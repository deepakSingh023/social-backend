package com.example.social_likes.util;

import org.springframework.security.core.Authentication;

public class SecurityUtil {

    public static String getCurrentUserId(Authentication authentication) {
        return authentication.getName(); // JWT subject / userId
    }
}
