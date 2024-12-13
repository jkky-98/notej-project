package com.github.jkky_98.noteJ.web.controller.form;

import lombok.Data;

@Data
public class CommentForm {

    private String content;
    private Long parentsId;
}
