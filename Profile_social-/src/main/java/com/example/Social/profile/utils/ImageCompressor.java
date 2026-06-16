package com.example.Social.profile.utils;

import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class ImageCompressor {

    /**
     * Compress an image to a small size while keeping good quality.
     * @param input InputStream of the image
     * @param targetWidth Desired width (keep 150-200 for profile pics)
     * @param quality JPEG compression quality (0.0f - 1.0f)
     * @return compressed image as byte array
     * @throws Exception
     */
    public static byte[] compress(InputStream input, int targetWidth, float quality) throws Exception {

        BufferedImage original = ImageIO.read(input);

        if (original == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        // Resize while maintaining aspect ratio
        BufferedImage resized = Scalr.resize(
                original,
                Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC,
                targetWidth,
                Scalr.OP_ANTIALIAS
        );

        // Prepare output stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // Get JPEG writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext())
            throw new IllegalStateException("No JPEG writer found");
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // e.g., 0.5f for very small

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(resized, null, null), param);
        }

        writer.dispose();

        return os.toByteArray();
    }

    /**
     * Default compression for profile pics: 150px width, quality 0.6
     */
    public static byte[] compressProfilePic(InputStream input) throws Exception {
        return compress(input, 150, 0.6f);
    }
}
