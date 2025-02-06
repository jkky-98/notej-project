package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

@Data
public class PostStatsForm {

    private Long totalViews;
    private Long todayViews;
    private Long yesterdayViews;
}
