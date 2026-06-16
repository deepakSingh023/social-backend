package com.example.Friend_Feed.dto;

import java.time.Instant;
import java.util.List;

public record PostResponse(
         String id,

        String userId,

        String avatar,

         String username,



         List<String>imageUrls,
         String videoUrl,

         String caption,
         String songUrl,
         String songName,
         String artistName,

        List<String> tags,

         boolean isPrivate,

         Instant createdAt,
         int comments ,

         int likes,

         Boolean isLiked
) {
}
