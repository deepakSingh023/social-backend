package com.example.social_chat.services;

import com.example.social_chat.utils.ImageCompressor;
import com.example.social_chat.utils.VideoCompressor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final VideoCompressor videoCompressor;
    private final ImageCompressor imageCompressor;
    private final R2Uploader uploader;


    public String processAndUpload(MultipartFile file) throws IOException {

        File processed;
        String contentType = file.getContentType();
        String key = UUID.randomUUID() + "-" + file.getOriginalFilename();

        if (contentType != null && contentType.startsWith("video")) {
            processed = videoCompressor.compressVideo(file);
        } else {
            processed = imageCompressor.compressImage(file);
        }

        return uploader.upload(processed, key, contentType);
    }
}
