package com.example.social_view.util;


import com.example.social_view.dto.ViewDto;
import com.example.social_view.entity.Reel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Set;

@FeignClient(name="reel-client" , url="${reel.uri}")
public interface ReelClient {

    @PutMapping("/api/reel/view-update")
    Set<String> getReel(
            @RequestBody ViewDto  data,
            @RequestHeader("X-SECRET-TOKEN") String token);

}
