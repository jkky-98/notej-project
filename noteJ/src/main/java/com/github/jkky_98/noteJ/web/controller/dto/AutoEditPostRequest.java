package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class AutoEditPostRequest {

    private String title;
    private String content;
    private List<String> tags;
    private String postUrl;
}
