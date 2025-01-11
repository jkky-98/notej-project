package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

@Data
public class PostHitsDto {
    private String username;
    private String postUrl;

    public PostHitsDto(String username, String postUrl) {
        this.username = username;
        this.postUrl = postUrl;
    }
}
