package com.example.social_post.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

public class VideoCompressor {

    static final String FFMPEG_PATH = "/usr/bin/ffmpeg"; // your FFmpeg path

    public static byte[] compress(MultipartFile file) throws Exception {

        File tempInput = File.createTempFile("input", ".mp4");
        File tempOutput = File.createTempFile("output", ".mp4");

        file.transferTo(tempInput);

        FFmpeg ffmpeg = new FFmpeg(FFMPEG_PATH);
        FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(tempInput.getAbsolutePath())
                .addOutput(tempOutput.getAbsolutePath())
                .setVideoBitRate(1_000_000) // 1mbps good quality
                .setAudioBitRate(128_000)
                .setConstantRateFactor(23)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        byte[] outputBytes = Files.readAllBytes(tempOutput.toPath());

        tempInput.delete();
        tempOutput.delete();

        return outputBytes;
    }
}
