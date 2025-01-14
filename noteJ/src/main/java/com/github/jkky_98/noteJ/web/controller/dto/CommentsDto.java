package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Comment;
import lombok.*;

import java.time.LocalDateTime;

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

    public static CommentsDto of(Comment comment) {
        return CommentsDto.builder()
                .id(comment.getId())
                .createBy(comment.getCreateBy())
                .createByDt(comment.getCreateDt())
                .content(comment.getContent())
                .parentsId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }
}
