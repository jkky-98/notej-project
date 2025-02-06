package com.github.jkky_98.noteJ.web.controller.form;

import lombok.Data;

@Data
public class LikeSaveRequestForm {
    private String postUrl;
    private boolean liked;
}
