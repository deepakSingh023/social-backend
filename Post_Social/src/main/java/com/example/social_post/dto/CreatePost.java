package com.example.social_post.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePost(
        String caption,

        List<String> images,// up to 7

        String video, // up to 40 seconds


        String songUrl,
        String songName,
        String artistName,

        List<String> tags,

        boolean isPrivate
) {
}
