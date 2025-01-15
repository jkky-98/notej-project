package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentForm {
    @NotBlank(message = "댓글을 입력하지 않았습니다.")
    private String content;
    private Long parentsId;
}
