package com.github.jkky_98.noteJ.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetLikeStatusToServiceDto {
    private String postUrl;
    private Long sessionUserId;
}
