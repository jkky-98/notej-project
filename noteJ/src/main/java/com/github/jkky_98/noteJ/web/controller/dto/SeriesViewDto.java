package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SeriesViewDto {
    private String seriesName;
    private int count;
    private LocalDateTime lastUpdateDt;
}
