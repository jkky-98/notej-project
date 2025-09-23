package me.notej.notej_api.core.comment.dto;

import me.notej.notej_api.core.comment.domain.Comment;
import me.notej.notej_api.core.member.dto.MemberSummary;

import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    Long postId,
    MemberSummary author,
    Long parentId,
    boolean isTopLevel,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPost().getId(),
                MemberSummary.fromEntity(comment.getMember()),
                // 부모 댓글이 있으면 ID, 없으면 null
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.isTopLevel(),
                comment.getCreated_at(),
                comment.getUpdated_at()
        );
    }
}
