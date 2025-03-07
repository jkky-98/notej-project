package com.github.jkky_98.noteJ.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminContactForm {
    private Long id;
    private String username;
    private String email;
    private String content;
    private LocalDateTime createDt;
}
