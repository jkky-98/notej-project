package com.github.jkky_98.noteJ.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeletePostToServiceDto {
    private String postUrl;
    private Long sessionUserId;
}
