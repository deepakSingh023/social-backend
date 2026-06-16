package com.example.Social.profile.tasks;


import com.example.Social.profile.dto.DenormalizeDto;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@org.springframework.cloud.openfeign.FeignClient(name="denormalizeComment", url="${comment.uri}")
public interface CommentsClient {

    @PutMapping("/api/comments/denormalize/update")
    void denormalizePost(
            @RequestBody DenormalizeDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
