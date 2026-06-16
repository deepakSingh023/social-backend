package com.example.social_post.dto;

public record UploadRequest(
        String fileName,
        String contentType
) {
}
