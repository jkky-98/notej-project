package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private boolean status;
    private String message;
    private String type;
    private String usernameSender;
    private LocalDateTime createTime;
}
