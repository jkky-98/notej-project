package com.github.jkky_98.noteJ.service.dto;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import lombok.Data;

@Data
public class WriteServiceEntityGenerateDto {
    private String title;
    private boolean writable;
    private String content;
    private String postSummary;
    private String url;
    private Series series;
    private String thumbnail;
    private User user;
}
