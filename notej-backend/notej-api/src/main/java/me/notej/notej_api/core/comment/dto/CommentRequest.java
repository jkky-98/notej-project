package me.notej.notej_api.core.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId,

        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,

        Long parentId
) {
}
