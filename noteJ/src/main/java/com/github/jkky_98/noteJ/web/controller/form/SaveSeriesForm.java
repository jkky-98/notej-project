package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveSeriesForm {
    @NotBlank(message = "시리즈 이름이 입력되지 않았습니다.")
    private String seriesName;
}
