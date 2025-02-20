package com.github.jkky_98.noteJ.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class DeleteLikeToServiceDto {
    private String postUrl;
    private boolean liked;
    private Long userId;

    public DeleteLikeToServiceDto() {
    }
}
