package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentsDto {

    private Long id;
    private String createBy;
    private LocalDateTime createByDt;
    private String content;
    private Long parentsId;
}
