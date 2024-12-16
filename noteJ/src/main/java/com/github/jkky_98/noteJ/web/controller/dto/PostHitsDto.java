package com.github.jkky_98.noteJ.web.controller.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class PostHitsDto {
    @Nullable
    private String username;
    private String postUrl;
}
