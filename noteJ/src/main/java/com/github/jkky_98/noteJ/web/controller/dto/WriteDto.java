package com.github.jkky_98.noteJ.web.controller.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WriteDto {
    private List<String> seriesNameList = new ArrayList<>();
}
