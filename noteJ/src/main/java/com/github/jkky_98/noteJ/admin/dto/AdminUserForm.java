package com.github.jkky_98.noteJ.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserForm {
    private Long userId;
    private String username;
    private String email;
    private String blogTitle;
    private String userBlogUrl;
    private LocalDateTime createDt;
}
