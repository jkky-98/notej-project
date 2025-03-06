package com.github.jkky_98.noteJ.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminContentsForm {

    private Long postId;
    private String username;
    private String postTitle;
    private String postUrl;
    private LocalDateTime createDt;
    private Integer viewCount;
    private Integer likeCount;

}
