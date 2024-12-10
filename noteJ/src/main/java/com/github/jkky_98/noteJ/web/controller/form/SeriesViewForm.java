package com.github.jkky_98.noteJ.web.controller.form;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SeriesViewForm {
    private String seriesName;
    private int count;
    private LocalDateTime lastUpdateDt;
}
