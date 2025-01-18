package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

@Data
public class LikeRequestDto {
    private String postUrl;
    private boolean liked;
}
