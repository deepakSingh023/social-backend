package com.example.social_view.controller;

import com.example.social_view.dto.ViewDto;
import com.example.social_view.service.ViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;





@RestController
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    @PostMapping("/create")
    public void createView(@RequestBody ViewDto data,
                           Authentication auth) {

        String userId = auth.getName();

        viewService.registerView(data,userId);
    }

    @PostMapping("/create-like-interest")
    public void createLikeInterest(@RequestParam String reelId,
                           @RequestParam String userId) {


        viewService.registerInterestLikes(reelId,userId);
    }


}
