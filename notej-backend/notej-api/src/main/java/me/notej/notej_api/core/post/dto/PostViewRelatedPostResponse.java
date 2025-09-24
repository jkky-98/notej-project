package me.notej.notej_api.core.post.dto;

import me.notej.notej_api.core.post.domain.Post;

import java.time.LocalDateTime;

public record PostViewRelatedPostResponse(
    Long id,
    String title,
    LocalDateTime updatedAt
) {
    public static PostViewRelatedPostResponse fromEntity(Post post) {
        return new PostViewRelatedPostResponse(
                post.getId(),
                post.getTitle(),
                post.getUpdated_at()
        );
    }
}
