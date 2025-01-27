package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class AutoSavePostRequest {

    private String title;
    private String content;
    private List<String> tags;
}
