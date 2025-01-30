package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Comment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentsDto {

    private Long id;
    private String createBy;
    private LocalDateTime createByDt;
    private String content;
    private Long parentsId;
    private List<CommentsDto> childrens;

    public static CommentsDto of(Comment comment) {
        return CommentsDto.builder()
                .id(comment.getId())
                .createBy(comment.getCreateBy())
                .createByDt(comment.getCreateDt())
                .content(comment.getContent())
                .childrens(comment.getChildrens().stream().map(CommentsDto::of).collect(Collectors.toList()))
                .parentsId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }
}
