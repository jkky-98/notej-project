package com.github.jkky_98.noteJ.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteLikeToServiceDto {
    private String postUrl;
    private boolean liked;
    private Long userId;
}
