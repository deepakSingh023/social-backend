package com.example.social_likes.util;

import com.example.social_likes.dto.IncrementDecDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "reel" , url = "${reel.service}")
public interface ReelClient {



    @PutMapping("/api/reel/like/inc")
    void likeInc(
            @RequestBody IncrementDecDto data,
            @RequestHeader("X-SECRET-TOKEN") String token
    );
}
