package com.example.social_interaction.tasks;
import com.example.social_interaction.dto.UpdateCounter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="denormalize", url="${profile.uri}")
public interface CounterClient {


    @PutMapping("/api/controller/counter/interaction")
    void denormalize(
            @RequestBody UpdateCounter data,
            @RequestHeader("X-SECRET-TOKEN") String token
            );


}
