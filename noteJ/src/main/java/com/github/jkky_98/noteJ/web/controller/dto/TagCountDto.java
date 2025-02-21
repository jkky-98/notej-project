package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagCountDto {
    private String tagName;
    private Long count;

    public TagCountDto() {
    }
}
