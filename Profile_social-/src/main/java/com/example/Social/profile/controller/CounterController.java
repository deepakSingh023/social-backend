package com.example.Social.profile.controller;


import com.example.Social.profile.dto.ReelUpdate;
import com.example.Social.profile.dto.UpdateCounter;
import com.example.Social.profile.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/controller/counter")
public class CounterController {

    private final CounterService counterService;



    @PutMapping
    public ResponseEntity<Void> updateCounter(
            @RequestBody UpdateCounter request
    ) {
        counterService.updateCounter(
                request.userId(),
                request.type(),
                request.delta()
        );

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/reel-number")
    public ResponseEntity<Void> updateReelCount(
            @RequestBody ReelUpdate data
            ){

        counterService.updateReelNumber(data.userId(), data.num());

        return ResponseEntity.noContent().build();

    }

    @PutMapping("/post-number")
    public ResponseEntity<Void> updatePostCount(
            @RequestBody ReelUpdate data
    ){

        counterService.updatePostNumber(data.userId(), data.num());

        return ResponseEntity.noContent().build();

    }

}
