package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PostDto {
    private String title;
    private String postSummary;
    private String postUrl;
    private String thumbnail;
    private boolean writable;
    private List<String> tags = new ArrayList<>();
    private int commentCount;
    private int likeCount;
    private LocalDateTime createByDt;
}