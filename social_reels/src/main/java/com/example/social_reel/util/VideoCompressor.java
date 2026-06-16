package com.example.social_reel.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

public class VideoCompressor {

    private static final String FFMPEG = "/usr/bin/ffmpeg";
    private static final String FFPROBE = "/usr/bin/ffprobe";

    public static File compressToFile(MultipartFile file) throws Exception {

        File input = File.createTempFile("input-", ".mp4");
        File output = File.createTempFile("output-", ".mp4");

        file.transferTo(input);

        FFmpeg ffmpeg = new FFmpeg(FFMPEG);
        FFprobe ffprobe = new FFprobe(FFPROBE);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(input.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(output.getAbsolutePath())
                .setVideoBitRate(1_000_000)
                .setAudioBitRate(128_000)
                .setConstantRateFactor(23)
                .done();

        new FFmpegExecutor(ffmpeg, ffprobe)
                .createJob(builder)
                .run();

        input.delete();
        return output;
    }
}
