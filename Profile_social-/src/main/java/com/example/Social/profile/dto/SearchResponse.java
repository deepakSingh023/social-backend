package com.example.Social.profile.dto;

import com.example.Social.profile.entity.profile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {

    private List<profile> profiles;

    private String nextCursor;

    private boolean hasMore;
}