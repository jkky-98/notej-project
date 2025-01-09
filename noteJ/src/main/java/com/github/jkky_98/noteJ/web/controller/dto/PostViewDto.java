package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostViewDto {

    private Long id;
    private String title;
    private String username;
    private LocalDateTime createByDt;
    private List<String> tags = new ArrayList<>();
    private String content;
    private int likeCount;
    private List<CommentsDto> comments = new ArrayList<>();
}
