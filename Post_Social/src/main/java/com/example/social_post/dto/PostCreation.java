package com.example.social_post.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class PostCreation {

    private String caption;

    private List<MultipartFile> images; // up to 7

    private MultipartFile video; // up to 40 seconds


    private String songUrl;
    private String songName;
    private String artistName;

    private List<String> tags;

    private boolean isPrivate; // public/private post
}
