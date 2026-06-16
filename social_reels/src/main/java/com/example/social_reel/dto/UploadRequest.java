package com.example.social_reel.dto;

public record UploadRequest(
        String fileName,
        String contentType
) {
}
