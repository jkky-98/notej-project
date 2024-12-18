package com.github.jkky_98.noteJ.web.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentForm {

    //toDo: 빈 값으로 업로드하면 안댐 테스트
    @NotBlank(message = "댓글을 입력해주세요.")
    private String content;
    private Long parentsId;
}
