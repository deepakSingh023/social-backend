package com.example.social_chat.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class VideoCompressor {

    public File compressVideo(MultipartFile file) throws IOException {

        File input = File.createTempFile("input-", ".mp4");
        file.transferTo(input);

        File output = File.createTempFile("output-", ".mp4");

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg",
                "-i", input.getAbsolutePath(),
                "-vcodec", "libx264",
                "-crf", "28",
                output.getAbsolutePath()
        );

        builder.inheritIO();
        Process process = builder.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return output;
    }
}
