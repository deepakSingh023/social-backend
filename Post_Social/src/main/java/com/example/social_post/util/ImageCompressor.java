package com.example.social_post.util;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCompressor {

   public static byte[] compress(byte[] input) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    Thumbnails.of(new ByteArrayInputStream(input))
            .scale(1.0)
            .outputFormat("jpg")   // important
            .outputQuality(0.7)
            .toOutputStream(baos);

    return baos.toByteArray();
}
}
