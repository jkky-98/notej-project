package com.github.jkky_98.noteJ.web.controller.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class WriteForm {
    private String title;
    private String tags;
    private String content;
    private MultipartFile thumbnail;
    private String postSummary;
    private boolean open;
    private String url;
    private String series;
}
