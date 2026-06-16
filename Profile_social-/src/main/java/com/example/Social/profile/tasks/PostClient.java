package com.example.Social.profile.tasks;


import com.example.Social.profile.dto.DenormalizeDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@org.springframework.cloud.openfeign.FeignClient(name="denormalizePost", url="${post.uri}")
public interface PostClient {

    @PutMapping("/api/denormalize/avatar")
    void denormalizePost(
            @RequestBody DenormalizeDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
