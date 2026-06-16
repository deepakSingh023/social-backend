package com.example.social_chat.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class ImageCompressor {

    public File compressImage(MultipartFile file) throws IOException {

        BufferedImage image = ImageIO.read(file.getInputStream());

        File compressed = File.createTempFile("img-", ".jpg");

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(compressed);

        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.6f);

        writer.write(null, new IIOImage(image, null, null), param);

        writer.dispose();
        ios.close();

        return compressed;
    }
}
