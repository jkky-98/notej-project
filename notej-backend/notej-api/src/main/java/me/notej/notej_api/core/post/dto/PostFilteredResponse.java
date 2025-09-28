package me.notej.notej_api.core.post.dto;

import me.notej.notej_api.core.category.dto.CategoryInPostResponse;
import me.notej.notej_api.core.post.domain.Post;

import java.time.LocalDateTime;

public record PostFilteredResponse(
        Long id,
        String title,
        String bio,
        String thumbnailUrl,
        LocalDateTime updatedAt,
        CategoryInPostResponse category,
        int commentCount
//        int viewCount
) {
    // Post 엔티티로부터 DTO를 생성하는 팩토리 메서드
    public static PostFilteredResponse fromEntity(Post post) {
        // Post 엔티티가 @Transactional 범위 내에 있기 때문에,
        // Lazy Loading된 연관 엔티티나 컬렉션 접근이 가능하다.

        // bio 필드가 Post 엔티티에 없을 경우, Post 엔티티의 summary 등을 사용하거나 기본값을 설정
        String postBio = (post.getBio() != null) ? post.getBio() : "";

        // thumbnailUrl 필드가 Post 엔티티에 없을 경우, 기본값 또는 다른 필드에서 유추
        String postThumbnailUrl = (post.getThumbnail() != null) ? post.getThumbnail() : null;

        return new PostFilteredResponse(
                post.getId(),
                post.getTitle(),
                postBio,
                postThumbnailUrl,
                post.getUpdated_at(),
                new CategoryInPostResponse(
                        post.getCategory().getId(),
                        post.getCategory().getName()
                ),
                post.getComments() != null ? post.getComments().size() : 0 // 댓글 컬렉션이 Lazy loading 될 수 있으므로 @Transactional이 중요
        );
    }
}
